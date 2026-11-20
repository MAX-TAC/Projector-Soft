package android.content;
public final class Intent {
    private ComponentName component;
    public Intent(String action){}
    public Intent setComponent(ComponentName component){this.component=component;return this;}
    public ComponentName getComponent(){return component;}
    public Intent putExtra(String key,int value){return this;}
}
