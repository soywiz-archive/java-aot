package flash.events;

import flash.Function;
import libcore.NativeClass;

@NativeClass
public interface IEventDispatcher {
    public void addEventListener(String type, Function listener, boolean useCapture/* = false*/, int priority/* = 0*/, boolean useWeakReference/* = false*/);

    public void removeEventListener(String type, Function listener, boolean useCapture/* = false*/);
    public boolean dispatchEvent(Event event);
    public boolean hasEventListener(String type);
    public boolean willTrigger(String type);
}
