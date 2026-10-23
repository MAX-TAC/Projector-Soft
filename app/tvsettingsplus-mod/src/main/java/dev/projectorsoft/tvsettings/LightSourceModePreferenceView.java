package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

/** Embeddable right-side OEM brightness selector; NOT a fullscreen Activity. */
public final class LightSourceModePreferenceView extends LinearLayout {
    private final LightSourceBrightnessController controller;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final RadioGroup choices;
    private final TextView message;
    private final Map<Integer,Integer> idMapping=new HashMap<>();
    private boolean updating;
    private int retry;
    private final Runnable verify=new Runnable() {
        @Override public void run() {
            LightSourceBrightnessController.Snapshot s=controller.refresh();
            if (s.pendingId!=null && retry++<4) {
                handler.postDelayed(this,750L);
            } else {
                if (s.pendingId!=null) controller.abandonUnconfirmedRequest();
                render();
            }
        }
    };

    public LightSourceModePreferenceView(Context ctx,LightSourceBrightnessController controller) {
        super(ctx);
        this.controller=controller;
        setOrientation(VERTICAL);
        TextView title=new TextView(ctx);
        title.setText("Яркость источника света");
        title.setTextSize(20);
        addView(title);
        choices=new RadioGroup(ctx);
        choices.setOrientation(VERTICAL);
        addView(choices);
        message=new TextView(ctx);
        addView(message);
        choices.setOnCheckedChangeListener((g,viewId)-> {
            if (updating) return;
            Integer mode=idMapping.get(viewId);
            if (mode==null) return;
            boolean dispatched=controller.select(mode);
            render();
            if (dispatched && controller.currentSnapshot().pendingId!=null) {
                handler.removeCallbacks(verify);
                retry=0;
                handler.postDelayed(verify,750L);
            }
        });
        controller.refresh();
        render();
    }

    public void render() {
        updating=true;
        try {
            LightSourceBrightnessController.Snapshot s=controller.currentSnapshot();
            setVisibility(s.enabled?View.VISIBLE:View.GONE);
            choices.removeAllViews();
            idMapping.clear();
            if (!s.enabled) return;
            for (LightSourceBrightnessController.Mode m:s.choices) {
                RadioButton b=new RadioButton(getContext());
                int viewId=View.generateViewId();
                b.setId(viewId);
                b.setText(m.title);
                b.setFocusable(true);
                b.setEnabled(s.pendingId==null);
                b.setChecked(s.currentId!=null && s.currentId.intValue()==m.hardwareId);
                idMapping.put(viewId,m.hardwareId);
                choices.addView(b);
            }
            message.setText(s.pendingId==null?"":"Ожидание подтверждения оборудования…");
        } finally {
            updating=false;
        }
    }
    @Override protected void onDetachedFromWindow() {
        handler.removeCallbacks(verify);
        super.onDetachedFromWindow();
    }
}
