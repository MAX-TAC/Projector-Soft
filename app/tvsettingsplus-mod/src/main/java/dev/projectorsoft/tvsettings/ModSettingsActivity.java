package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** A standalone TV focus surface with a left section rail and right settings pane. */
public abstract class ModSettingsActivity extends Activity {
    private static final int NAV_BASE=100;
    private static final int ROW_BASE=1000;
    private static final int BACKGROUND=0xff15191d;
    private static final int PANEL=0xff20252a;
    private static final int FOCUS=0xff485e70;
    private static final int SECONDARY=0xffd0d8df;

    private ModMenuCatalog.Page selected;
    private LinearLayout content;
    private LinearLayout nav;
    private ScrollView right;
    private TextView heading;
    private ModMenuBackend backend;
    private int restoreFocus=View.NO_ID;

    /** Subclasses select a fixed page. The root previews all pages. */
    abstract ModMenuCatalog.Page page();

    @Override public void onCreate(Bundle state) {
        setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
        super.onCreate(state);
        // Intent action, extras and URI are intentionally ignored even for exported screens.
        selected=page()==null?ModMenuCatalog.Page.NETWORK:page();
        if(state!=null) {
            int ordinal=state.getInt("selected",selected.ordinal());
            if(page()==null && ordinal>=0 && ordinal<ModMenuCatalog.Page.values().length)
                selected=ModMenuCatalog.Page.values()[ordinal];
            restoreFocus=state.getInt("focus",View.NO_ID);
        }
        setTitle(page()==null?"Настройки Android TV":page().title);
        LinearLayout layout=column();
        layout.setBackgroundColor(BACKGROUND);
        layout.setPadding(dp(24),dp(18),dp(24),dp(16));
        TextView title=text("Настройки Android TV",28,Color.WHITE);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        layout.addView(title,new LinearLayout.LayoutParams(-1,dp(52)));

        LinearLayout panes=new LinearLayout(this);
        panes.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(panes,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView rail=new ScrollView(this);
        rail.setFillViewport(true);
        rail.setBackgroundColor(PANEL);
        panes.addView(rail,new LinearLayout.LayoutParams(0,-1,32));
        nav=column();
        nav.setPadding(dp(8),dp(8),dp(8),dp(12));
        rail.addView(nav);
        LinearLayout rightPane=column();
        rightPane.setPadding(dp(18),0,0,0);
        panes.addView(rightPane,new LinearLayout.LayoutParams(0,-1,68));
        heading=text("",24,Color.WHITE);
        heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        rightPane.addView(heading,new LinearLayout.LayoutParams(-1,dp(48)));
        right=new ScrollView(this);
        right.setFillViewport(true);
        rightPane.addView(right,new LinearLayout.LayoutParams(-1,0,1));
        content=column();
        content.setPadding(dp(2),0,dp(6),dp(8));
        right.addView(content);
        for(ModMenuCatalog.Page item:ModMenuCatalog.Page.values()) {
            TextView entry=rowText(item.title,"");
            entry.setId(NAV_BASE+item.ordinal());
            entry.setOnClickListener(view->openPage(item));
            entry.setOnFocusChangeListener((view,focused)->{
                if(focused && page()==null && selected!=item){
                    selected=item;
                    showContent(View.NO_ID);
                    right.scrollTo(0,0);
                }
            });
            nav.addView(entry,rowParams());
        }
        setContentView(layout);
    }

    @Override protected void onResume() {
        super.onResume();
        backend=null;
        int target=restoreFocus==View.NO_ID ? NAV_BASE+selected.ordinal() : restoreFocus;
        showContent(target);
        restoreFocus=View.NO_ID;
    }

    @Override protected void onPause() {
        restoreFocus=focusedId();
        super.onPause();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("selected",selected.ordinal());
        int focused=focusedId();
        out.putInt("focus",focused==View.NO_ID?restoreFocus:focused);
        super.onSaveInstanceState(out);
    }

    private void showContent(int focus) {
        heading.setText(selected.title);
        content.removeAllViews();
        int index=0;
        if(page()==null) {
            addRow(index++,"Открыть раздел","",()->openPage(selected));
            for(ModMenuCatalog.Row spec:ModMenuCatalog.rows(selected)) {
                if(spec.kind!=ModMenuCatalog.Kind.GROUP)
                    addRow(index++,spec.title,"",null);
            }
        } else {
            if(backend==null) {
                backend=new ModMenuBackend(this,selected);
                backend.load();
            }
            for(ModMenuCatalog.Row spec:ModMenuCatalog.rows(selected)) {
                if(spec.kind==ModMenuCatalog.Kind.GROUP) {
                    TextView group=text(spec.title,16,SECONDARY);
                    group.setPadding(dp(12),dp(12),dp(12),dp(4));
                    content.addView(group);
                } else {
                    ModMenuBackend.State state=backend.state(spec,()->{
                        if(!isFinishing() && !isDestroyed())showContent(focusedId());
                    });
                    addRow(index++,spec.title,state.detail,state.action);
                }
            }
        }
        for(int i=0;i<nav.getChildCount();i++){
            View item=nav.getChildAt(i);
            item.setActivated(i==selected.ordinal());
            item.setNextFocusRightId(ROW_BASE);
        }
        if(focus!=View.NO_ID) {
            View target=findViewById(focus);
            if(target!=null)target.requestFocus();
        }
    }

    private void addRow(int index,String title,String detail,Runnable action) {
        TextView view=rowText(title,detail);
        view.setId(ROW_BASE+index);
        view.setNextFocusLeftId(NAV_BASE+selected.ordinal());
        if(action==null){
            view.setClickable(false);
            view.setTextColor(SECONDARY);
        } else {
            view.setOnClickListener(ignored->action.run());
        }
        content.addView(view,rowParams());
    }

    private void openPage(ModMenuCatalog.Page target) {
        if(target==page()){
            View first=findViewById(ROW_BASE);
            if(first!=null)first.requestFocus();
            return;
        }
        if(open(new Intent(this,activityFor(target)))) {
            selected=target;
            if(page()!=null)finish();
        }
    }

    @Override public boolean onKeyUp(int code,KeyEvent event) {
        if(code==KeyEvent.KEYCODE_SETTINGS) {
            if(page()!=null && open(new Intent(this,ModMainSettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP)))finish();
            return true;
        }
        if(code==KeyEvent.KEYCODE_TV_INPUT) {
            openPage(ModMenuCatalog.Page.SOURCES);
            return true;
        }
        return super.onKeyUp(code,event);
    }

    private boolean open(Intent intent) {
        try {
            startActivity(intent);
            return true;
        }catch(ActivityNotFoundException | SecurityException failure){
            android.util.Log.w("TvSettingsPlusMOD","Unified settings section unavailable",failure);
            Toast.makeText(this,"Раздел недоступен",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    static Class<? extends Activity> activityFor(ModMenuCatalog.Page target) {
        switch(target) {
            case NETWORK: return NetworkSettingsActivity.class;
            case BLUETOOTH: return BluetoothSettingsActivity.class;
            case SOURCES: return SourcesSettingsActivity.class;
            case PROJECTION: return ProjectionSettingsActivity.class;
            case PICTURE: return PictureSettingsActivity.class;
            case SOUND: return SoundSettingsActivity.class;
            case APPS: return AppsSettingsActivity.class;
            case SYSTEM: return SystemSettingsActivity.class;
            default: throw new AssertionError(target);
        }
    }

    private int focusedId() {
        View focused=getCurrentFocus();
        return focused==null?View.NO_ID:focused.getId();
    }

    private TextView rowText(String label,String detail) {
        TextView row=text(detail.isEmpty()?label:label+"\n"+detail,18,Color.WHITE);
        row.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        row.setPadding(dp(12),dp(8),dp(12),dp(8));
        row.setMinHeight(dp(50));
        row.setFocusable(true);
        row.setFocusableInTouchMode(true);
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused},tile(FOCUS));
        states.addState(new int[]{android.R.attr.state_activated},tile(0xff303943));
        states.addState(new int[0],tile(PANEL));
        row.setBackground(states);
        return row;
    }

    private GradientDrawable tile(int color) {
        GradientDrawable background=new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(7));
        return background;
    }

    private LinearLayout.LayoutParams rowParams(){
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,-2);
        params.bottomMargin=dp(4);
        return params;
    }
    private LinearLayout column() {
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }
    private TextView text(String label,int size,int color) {
        TextView view=new TextView(this);
        view.setText(label);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }
    private int dp(int value){
        return Math.round(value*getResources().getDisplayMetrics().density);
    }
}
