package libgame.as3;

import jflash.backend.Component;
import jflash.backend.RawEngineContext;

public class As3EngineContext extends RawEngineContext {
    @Override
    public void loop(final Component root) {
        final As3EngineContext context = this;

        As3Native.onEnterFrame(new Runnable() {
            @Override
            public void run() {
                root.update(20);
                root.render(context);
            }
        });
    }
}
