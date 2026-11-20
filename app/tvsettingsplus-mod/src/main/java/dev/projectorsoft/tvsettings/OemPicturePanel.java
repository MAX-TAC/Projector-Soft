package dev.projectorsoft.tvsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.WeakHashMap;

/** Native Android TV rows for the picture state stored by the original backend. */
final class OemPicturePanel {
    static final String STATUS="projectorsoft_picture_status";
    private static final String PREFIX="projectorsoft_picture_read_";
    private static final Set<String> COLOR = Set.of(PicturePanelState.TEMPERATURE,
            "picture_red_gain", "picture_green_gain", "picture_blue_gain");
    private static final String TAG="TvSettingsPlusMOD";
    private static final Map<Object,WeakReference<Binding>> BINDINGS=new WeakHashMap<>();

    private OemPicturePanel(){}

    static void install(Context context,Object group,Class<?> pref) throws ReflectiveOperationException {
        installRows(context, group, pref, key -> true, true);
    }

    static void installSection(Context context, Object group, Class<?> pref, boolean colorOnly)
            throws ReflectiveOperationException {
        installRows(context, group, pref, key -> COLOR.contains(key) == colorOnly, !colorOnly);
    }

    private static void installRows(Context context, Object group, Class<?> pref,
                                    Predicate<String> include, boolean includeReset)
            throws ReflectiveOperationException {
        row(context,group,pref,STATUS,"Состояние изображения","Подготовка значений…",210);
        for(int i=0;i<PicturePanelState.KEYS.length;i++){
            if (!include.test(PicturePanelState.KEYS[i])) continue;
            row(context,group,pref,PREFIX+PicturePanelState.KEYS[i],
                    PicturePanelState.TITLES[i],"Ожидание чтения",211+i);
        }
        if (includeReset) row(context,group,pref,"projectorsoft_picture_reset_state","Сброс параметров изображения",
                "Недоступен до подтверждения штатной процедуры для активного профиля",230);
    }

    private static void row(Context context,Object group,Class<?> pref,String key,String title,String summary,int order)
            throws ReflectiveOperationException {
        Object item=pref.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,key);
        pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
        pref.getMethod("setOrder",int.class).invoke(item,order);
        pref.getMethod("setSelectable",boolean.class).invoke(item,false);
        pref.getMethod("setSummary",CharSequence.class).invoke(item,summary);
        group.getClass().getMethod("addPreference",pref).invoke(group,item);
    }

    static void bind(Object fragment,View root){
        WeakReference<Binding> reference=BINDINGS.get(fragment);
        Binding previous=reference==null?null:reference.get();
        if(previous!=null && previous.root==root)return;
        if(previous!=null)previous.close();
        BINDINGS.remove(fragment);
        if(root==null || !"8054".equals(OemHdmiPortInfo.machineId()))return;
        try {
            Method find=fragment.getClass().getMethod("findPreference",CharSequence.class);
            if(find.invoke(fragment,STATUS)==null)return;
            Binding binding=new Binding(fragment,root,find);
            BINDINGS.put(fragment,new WeakReference<>(binding));
            root.addOnAttachStateChangeListener(binding);
            if(root.isAttachedToWindow())binding.onViewAttachedToWindow(root);
        }catch(ReflectiveOperationException | RuntimeException ex){
            Log.w(TAG,"Picture panel binding unavailable",ex);
        }
    }

    private static final class Binding extends ContentObserver implements View.OnAttachStateChangeListener,
            ViewTreeObserver.OnWindowFocusChangeListener {
        private final Object fragment;
        private final View root;
        private final Method find;
        private final Method summary;
        private final ContentResolver resolver;
        private boolean active;

        Binding(Object fragment,View root,Method find) throws ReflectiveOperationException {
            super(new Handler(Looper.getMainLooper()));
            this.fragment=fragment;
            this.root=root;
            this.find=find;
            this.summary=Class.forName("androidx.preference.Preference").getMethod("setSummary",CharSequence.class);
            this.resolver=root.getContext().getContentResolver();
        }

        @Override public void onViewAttachedToWindow(View view){
            if(active)return;
            active=true;
            view.getViewTreeObserver().addOnWindowFocusChangeListener(this);
            PicturePanelState.observe(key ->
                    resolver.registerContentObserver(Settings.Global.getUriFor(key),false,this),
                    (key, error) -> Log.w(TAG,"Picture observer unavailable for " + key,error));
            refresh();
        }

        @Override public void onViewDetachedFromWindow(View view){
            if(!active)return;
            active=false;
            ViewTreeObserver observer=view.getViewTreeObserver();
            if(observer.isAlive())observer.removeOnWindowFocusChangeListener(this);
            try{resolver.unregisterContentObserver(this);}
            catch(RuntimeException ex){Log.w(TAG,"Cannot detach picture observer",ex);}
        }

        void close(){
            onViewDetachedFromWindow(root);
            root.removeOnAttachStateChangeListener(this);
        }

        @Override public void onChange(boolean selfChange){refresh();}
        @Override public void onWindowFocusChanged(boolean focused){if(focused)refresh();}

        private void refresh(){
            if(!active)return;
            Map<String,Integer> values=null;
            try{values=PicturePanelState.read(key->OemPictureState.readStored(root.getContext(),key));}
            catch(RuntimeException ex){Log.w(TAG,"Cannot read stored picture state",ex);}
            try{
                set(STATUS,values==null?"Снимок недоступен или режим изменился при чтении"
                        :"Сохранённые значения Android. Вход и профиль не подтверждены; изменение недоступно");
                for(String key:PicturePanelState.KEYS){
                    Integer value=values==null?null:values.get(key);
                    set(PREFIX+key,value==null?"Недоступно":value.toString());
                }
            }catch(ReflectiveOperationException ex){Log.w(TAG,"Cannot refresh picture rows",ex);}
        }

        private void set(String key,String value) throws ReflectiveOperationException {
            Object item=find.invoke(fragment,key);
            if(item!=null)summary.invoke(item,value);
        }
    }
}
