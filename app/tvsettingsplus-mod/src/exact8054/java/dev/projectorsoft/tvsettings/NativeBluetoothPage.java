package dev.projectorsoft.tvsettings;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.android.tv.settings.accessories.AccessoriesFragment;

/** Keeps the STOCK accessory rows and editor on the primary Bluetooth screen. */
public final class NativeBluetoothPage extends AccessoriesFragment {
    static final String SWITCH = "projectorsoft_bluetooth";
    static final String BONDED = "projectorsoft_bonded_devices";
    static final String ADD = "add_accessory";
    static final String WARNING = "Отключение Bluetooth может разорвать соединение с пультом управления "
            + "и подключёнными аудиоустройствами. Продолжить?";
    private static final String BLOCKED = "Способ управления без Bluetooth не подтверждён";
    private static final String TAG = "TvSettingsPlusMOD";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private PreferenceCategory devices;
    private SwitchPreference toggle;
    private boolean registered;
    private boolean enabling;
    private final Runnable enableTimeout = () -> {
        enabling = false;
        refreshToggle("Включение Bluetooth не подтверждено");
    };
    private final BroadcastReceiver changes = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) refreshAccessories();
            refreshToggle(null);
        }
    };

    // Called from the pinned stock updateAccessories(), including its broadcast paths.
    public static PreferenceGroup deviceRows(PreferenceFragmentCompat fragment) {
        return fragment instanceof NativeBluetoothPage ? ((NativeBluetoothPage) fragment).devices
                : fragment.getPreferenceScreen();
    }

    @Override public void onCreatePreferences(Bundle state, String rootKey) {
        super.onCreatePreferences(state, rootKey);
        PreferenceScreen screen = getPreferenceScreen();
        Context context = requireContext();
        screen.setTitle("Bluetooth");
        toggle = new SwitchPreference(context);
        toggle.setKey(SWITCH);
        toggle.setTitle("Bluetooth");
        toggle.setPersistent(false);
        toggle.setOrder(0);
        toggle.setOnPreferenceChangeListener((preference, next) -> {
            if (next instanceof Boolean) request((Boolean) next);
            return false;
        });
        screen.addPreference(toggle);

        devices = new PreferenceCategory(context);
        devices.setKey(BONDED);
        devices.setTitle("Сопряжённые устройства");
        devices.setOrder(10);
        screen.addPreference(devices);

        Preference add = screen.findPreference(ADD);
        if (add != null) {
            add.setTitle("Добавить устройство");
            add.setOrder(20);
        }
        refreshToggle(null);
    }

    @Override public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        try {
            requireContext().registerReceiver(changes, filter, Context.RECEIVER_EXPORTED);
            registered = true;
        } catch (RuntimeException unavailable) {
            Log.w(TAG, "Bluetooth state receiver unavailable", unavailable);
        }
    }

    @Override public void onResume() {
        super.onResume();
        refreshToggle(null);
    }

    @Override public void onStop() {
        if (registered) {
            requireContext().unregisterReceiver(changes);
            registered = false;
        }
        handler.removeCallbacks(enableTimeout);
        enabling = false;
        super.onStop();
    }

    private void refreshAccessories() {
        try {
            java.lang.reflect.Method update = AccessoriesFragment.class.getDeclaredMethod("updateAccessories");
            update.setAccessible(true);
            update.invoke(this);
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            Log.w(TAG, "Cannot update stock bonded devices", unavailable);
        }
    }

    private BluetoothAdapter adapter() {
        BluetoothManager manager = requireContext().getSystemService(BluetoothManager.class);
        return manager == null ? null : manager.getAdapter();
    }

    private boolean mayChange(Context context) {
        UserManager users = context.getSystemService(UserManager.class);
        int flags = context.getApplicationInfo().flags;
        return "8054".equals(OemHdmiPortInfo.machineId())
                && (flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
                && context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && users != null && !users.hasUserRestriction(UserManager.DISALLOW_BLUETOOTH)
                && !users.hasUserRestriction(UserManager.DISALLOW_CONFIG_BLUETOOTH);
    }

    @SuppressWarnings("deprecation")
    private void request(boolean enable) {
        try {
            BluetoothAdapter adapter = adapter();
            if (adapter == null || !mayChange(requireContext())) {
                refreshToggle("Изменение Bluetooth недоступно");
            } else if (!enable && adapter.getState() == BluetoothAdapter.STATE_ON) {
                // The remote is IR, not Bluetooth, so switching the radio off
                // cannot lock the user out. No warning, just do it.
                disableNow(adapter);
            } else if (enable && adapter.getState() == BluetoothAdapter.STATE_OFF) {
                enabling = adapter.enable();
                if (enabling) handler.postDelayed(enableTimeout, 15000);
                refreshToggle(enabling ? null : "Не удалось включить Bluetooth");
            }
        } catch (RuntimeException refused) {
            Log.w(TAG, "Bluetooth enable request refused", refused);
            refreshToggle("Изменение Bluetooth недоступно");
        }
    }

    @SuppressWarnings("deprecation")
    private void disableNow(BluetoothAdapter adapter) {
        try {
            if (adapter.disable()) {
                refreshToggle(null);
            } else {
                refreshToggle("Не удалось отключить Bluetooth");
            }
        } catch (RuntimeException refused) {
            Log.w(TAG, "Bluetooth disable request refused", refused);
            refreshToggle("Изменение Bluetooth недоступно");
        }
    }

    private void refreshToggle(String failure) {
        if (toggle == null || !isAdded()) return;
        try {
            BluetoothAdapter adapter = adapter();
            int state = adapter == null ? BluetoothAdapter.ERROR : adapter.getState();
            if (state == BluetoothAdapter.STATE_ON && enabling) {
                enabling = false;
                handler.removeCallbacks(enableTimeout);
            }
            toggle.setChecked(state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_TURNING_OFF);
            toggle.setEnabled(!enabling && mayChange(requireContext())
                    && (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF));
            String summary;
            if (state == BluetoothAdapter.STATE_TURNING_ON || enabling) summary = "Включается…";
            else if (state == BluetoothAdapter.STATE_TURNING_OFF) summary = "Выключается…";
            else if (state == BluetoothAdapter.STATE_ON) summary = "Включён";
            else if (state == BluetoothAdapter.STATE_OFF) summary = "Выключен";
            else summary = "Состояние Bluetooth недоступно";
            toggle.setSummary(failure == null ? summary : failure);
        } catch (RuntimeException unavailable) {
            toggle.setEnabled(false);
            toggle.setSummary("Состояние Bluetooth недоступно");
        }
    }
}
