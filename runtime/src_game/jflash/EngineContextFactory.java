package jflash;

import jflash.backend.EngineContext;
import jflash.backend.swing.SwingEngineContext;
import libcore.NativeClass;
import libcore.NativeMethod;

@NativeClass
public class EngineContextFactory {
    @NativeMethod
    static public EngineContext create() {
        //return new SwingEngineContext(800, 600);
        return null;
    }
}
