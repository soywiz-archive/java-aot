package flash.events;

import flash.Function;
import libcore.NativeClass;

@NativeClass
public class EventDispatcher implements IEventDispatcher {
    native public void addEventListener(String type, Function listener, boolean useCapture, int priority, boolean useWeakReference);
    native public void removeEventListener(String type, Function listener, boolean useCapture);
    native public boolean dispatchEvent(Event event);
    native public boolean hasEventListener(String type);
    native public boolean willTrigger(String type);
}
