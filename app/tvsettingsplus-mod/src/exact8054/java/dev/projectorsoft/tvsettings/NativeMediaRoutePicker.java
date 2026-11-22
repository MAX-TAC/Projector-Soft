package dev.projectorsoft.tvsettings;

import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.PreferenceScreen;
import java.util.ArrayList;
import java.util.List;

/**
 * One-at-a-time remote/D-pad controlled output chooser.
 * The only write is MEDIA preferred AudioProductStrategy; original preference is
 * restored when validation fails, user cancels, activity stops or the test times out.
 */
final class NativeMediaRoutePicker {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final int ROUTE_CHECK_MS = 12000;
    private static final int FALLBACK_CHECK_MS = 1100;
    private static final int CONFIRM_MS = 25000;
    private static final int POLL_MS = 250;
    private static Pending active;

    private NativeMediaRoutePicker() {}

    static void show(Context context, PreferenceScreen screen) {
        if (active != null) {
            Toast.makeText(context, "Сначала завершите текущую проверку аудиовыхода",
                    Toast.LENGTH_LONG).show();
            return;
        }
        try {
            NativeMediaRouteBridge bridge = NativeMediaRouteBridge.open(context);
            List<NativeMediaRouteBridge.Output> outputs = bridge.outputs();
            if (outputs.isEmpty()) {
                Toast.makeText(context, "Нет доступных аудиовыходов", Toast.LENGTH_LONG).show();
                return;
            }
            List<String> entries = new ArrayList<>();
            entries.add("Автоматически");
            for (NativeMediaRouteBridge.Output output : outputs) {
                entries.add(output.label);
            }
            new AlertDialog.Builder(context)
                    .setTitle("Устройство вывода звука")
                    .setItems(entries.toArray(new CharSequence[0]), (dialog, which) -> {
                        NativeMediaRouteBridge.Output target =
                                which == 0 ? null : outputs.get(which - 1);
                        begin(context, screen, bridge, target);
                    })
                    .setNegativeButton("Закрыть", null)
                    .show();
        } catch (Exception | LinkageError unavailable) {
            Log.w(TAG, "System MEDIA routing API unavailable", unavailable);
            Toast.makeText(context, "Переключение недоступно: проверьте разрешение "
                    + "MODIFY_AUDIO_ROUTING и журнал приложения", Toast.LENGTH_LONG).show();
        }
    }

    private static void begin(Context context, PreferenceScreen screen,
                              NativeMediaRouteBridge bridge,
                              NativeMediaRouteBridge.Output target) {
        if (active != null) return;
        try {
            NativeMediaRouteTransaction transaction =
                    new NativeMediaRouteTransaction(bridge, target);
            if (transaction.noChangeNeeded()) {
                Toast.makeText(context, "Автоматическая маршрутизация уже выбрана",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!transaction.targetAvailable()) {
                Toast.makeText(context, "Устройство отключилось. Обновите список",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Pending pending = new Pending(context, screen, transaction, target);
            pending.open();
        } catch (Exception | LinkageError unavailable) {
            Log.w(TAG, "Unable to prepare safe MEDIA route transaction", unavailable);
            Toast.makeText(context, "Не удалось сохранить исходный маршрут. Запись отменена",
                    Toast.LENGTH_LONG).show();
        }
    }

    static void cancelOnStop() {
        if (active != null) active.dialog.dismiss();
    }

    private static final class Pending {
        final Context context;
        final PreferenceScreen screen;
        final NativeMediaRouteTransaction transaction;
        final NativeMediaRouteBridge.Output target;
        final Handler main = new Handler(Looper.getMainLooper());
        final AlertDialog dialog;
        final Runnable poll = this::sample;
        boolean changed;
        boolean approved;
        boolean confirmedRoute;
        boolean fallbackRequested;
        long startedAt, confirmedAt, lastDiagnosticAt;
        String failure;

        Pending(Context context, PreferenceScreen screen,
                NativeMediaRouteTransaction transaction,
                NativeMediaRouteBridge.Output target) {
            this.context = context;
            this.screen = screen;
            this.transaction = transaction;
            this.target = target;
            dialog = new AlertDialog.Builder(context)
                    .setTitle("Переключение звука")
                    .setMessage("Подготовка маршрута…")
                    .setPositiveButton("Звук работает", (d, which) -> { approved = confirmedRoute; })
                    .setNegativeButton("Отменить и вернуть", (d, which) -> {})
                    .create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnDismissListener(ignored -> finish());
        }

        void open() {
            if (active != null) return;
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            active = this;
            startedAt = SystemClock.uptimeMillis();
            try {
                // Mark before the call: a vendor implementation may throw after partial apply.
                changed = true;
                if (!transaction.apply()) {
                    failure = "AudioPolicy не приняла новый выход";
                    dialog.dismiss();
                    return;
                }
                Log.i(TAG, "MEDIA route requested: " + (target == null ? "AUTO" : target.label));
                main.postDelayed(poll, POLL_MS);
            } catch (Exception | LinkageError problem) {
                Log.w(TAG, "MEDIA output route write failed", problem);
                failure = "Не удалось установить аудиомаршрут";
                dialog.dismiss();
            }
        }

        private void sample() {
            if (active != this || approved || !dialog.isShowing()) return;
            try {
                if (!transaction.bluetoothPreserved()) {
                    failure = "Bluetooth-устройство отключилось — исходный маршрут восстановлен";
                    dialog.dismiss();
                    return;
                }
                if (!transaction.targetAvailable()) {
                    failure = "Выбранный выход исчез — исходный маршрут восстановлен";
                    dialog.dismiss();
                    return;
                }
                boolean settled = transaction.confirmed();
                long now = SystemClock.uptimeMillis();
                if (now - lastDiagnosticAt >= 900) {
                    lastDiagnosticAt = now;
                    Log.i(TAG, "MEDIA switch elapsed=" + (now - startedAt) + "ms target="
                            + (target == null ? "Automatic" : target.label)
                            + " settled=" + settled + " " + transaction.diagnostic());
                }
                if (!settled && !fallbackRequested && target != null
                        && target.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                        && now - startedAt >= FALLBACK_CHECK_MS) {
                    // Evidence on 8054: preferred Speaker is accepted while
                    // actual MEDIA keeps playing via A2DP. Force exclusion
                    // through AudioService, with separately captured rollback.
                    fallbackRequested = transaction.trySpeakerForceFallback();
                    if (fallbackRequested) Log.i(TAG, "MEDIA Speaker A2DP exclusion requested");
                }
                if (!confirmedRoute) {
                    if (settled) {
                        confirmedRoute = true;
                        confirmedAt = now;
                        Log.i(TAG, "MEDIA route readback confirmed: " + (target == null ? "AUTO" : target.label));
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else if (now - startedAt >= ROUTE_CHECK_MS) {
                        failure = "Звук не переключился — прежний выход восстановлен";
                        Log.w(TAG, "MEDIA route failed after alternate policy; target="
                                + (target == null ? "AUTO" : target.label));
                        dialog.dismiss();
                        return;
                    } else {
                        dialog.setMessage("Переключение звука…"
                                + "\nПри неудаче прежний выход восстановится автоматически.");
                    }
                }
                if (confirmedRoute) {
                    if (!settled) {
                        failure = "Аудиомаршрут изменился извне — исходные настройки восстановлены";
                        dialog.dismiss();
                        return;
                    }
                    long remaining = (CONFIRM_MS - (now - confirmedAt) + 999) / 1000;
                    if (remaining <= 0) {
                        failure = "Время подтверждения истекло — выполнен откат";
                        dialog.dismiss();
                        return;
                    }
                    dialog.setMessage((target == null ? "Автоматический вывод"
                                    : target.label)
                            + "\nПроверьте, что звук действительно идёт через нужное устройство."
                            + "\nПодтвердите кнопкой «Звук работает»."
                            + "\nАвтоматический откат через " + remaining + " с.");
                }
                main.postDelayed(poll, confirmedRoute ? 1000 : POLL_MS);
            } catch (Exception | LinkageError error) {
                Log.w(TAG, "MEDIA route readback unavailable", error);
                failure = "Системный readback недоступен — выполнен откат";
                dialog.dismiss();
            }
        }

        private void finish() {
            main.removeCallbacks(poll);
            if (active == this) active = null;
            if (approved) {
                Toast.makeText(context, "Аудиовыход выбран. Bluetooth не отключался",
                        Toast.LENGTH_LONG).show();
            } else if (changed) {
                boolean restored = false;
                try { restored = transaction.rollback(); }
                catch (Exception | LinkageError error) {
                    Log.e(TAG, "MEDIA route rollback failed", error);
                }
                if (!restored) {
                    Log.e(TAG, "MEDIA route rollback NOT verified");
                    Toast.makeText(context, "ВНИМАНИЕ: откат не подтверждён. "
                            + "Верните аудиовыход через штатные настройки", Toast.LENGTH_LONG).show();
                } else {
                    Log.i(TAG, "MEDIA preferred strategy restored; original media force="
                            + transaction.originalForce());
                    Toast.makeText(context, failure == null
                            ? "Исходный аудиомаршрут восстановлен" : failure,
                            Toast.LENGTH_LONG).show();
                }
            }
            NativeAudioOutputReadout.refresh(context, screen);
        }
    }
}
