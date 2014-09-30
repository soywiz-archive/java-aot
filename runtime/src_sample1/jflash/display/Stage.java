package jflash.display;

import jflash.backend.EngineContext;
import jflash.util.Color;

public class Stage extends DisplayObjectContainer {
    private EngineContext context;

    public Stage(EngineContext context) {
        this.context = context;
    }

    public Color backgroundColor = Color.black;

    @Override
    public Stage stage() {
        return this;
    }

    public void loop() {
        context.loop(this);
    }

    @Override
    protected void renderInternal(EngineContext context) {
        context.clear(backgroundColor);
        super.renderInternal(context);
    }
}