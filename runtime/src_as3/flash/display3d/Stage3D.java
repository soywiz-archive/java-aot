package flash.display3d;

import libcore.NativeClass;
import libcore.Property;
import flash.events.EventDispatcher;

@NativeClass
public class Stage3D extends EventDispatcher {
    native public Context3D context3D();
    native public void requestContext3D(String context3DRenderMode/* = "auto"*/, String profile/* = "baseline"*/);
    native public void requestContext3DMatchingProfiles(String[] profiles);

    public double x;
    public double y;
    public boolean visible;
    /*
    @Property native public double x();
    @Property native public void x(double value);

    @Property native public double y();
    @Property native public void y(double value);

    @Property native public boolean visible();
    @Property native public void visible(boolean value);
    */
}
