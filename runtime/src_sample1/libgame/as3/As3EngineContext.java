package libgame.as3;

import jflash.backend.Component;
import jflash.backend.RawEngineContext;
import jflash.backend.State;
import jflash.util.Color;
import jflash.util.Point;

public class As3EngineContext extends RawEngineContext {
    @Override
    public void clear(Color color) {
        As3Native.clear(color.toInt());
    }

    @Override
    public void present() {
        As3Native.present();
    }

    private Point p1 = new Point(), p2 = new Point(), p3 = new Point(), p4 = new Point();

    @Override
    public void drawSolid(int width, int height, Color color) {
        State state = this.states.get();
        p1 = state.matrix.transformPoint(p1.setXY(0, 0), p1);
        p2 = state.matrix.transformPoint(p2.setXY(width, 0), p2);
        p3 = state.matrix.transformPoint(p3.setXY(width, height), p3);
        p4 = state.matrix.transformPoint(p4.setXY(0, height), p4);

        float[] data = new float[] {
            p1.x, p1.y,
            p2.x, p2.y,
            p3.x, p3.y,

            p1.x, p1.y,
            p3.x, p3.y,
            p4.x, p4.y,
        };

        As3Native.drawTriangles(color.toInt(), data);
    }

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
