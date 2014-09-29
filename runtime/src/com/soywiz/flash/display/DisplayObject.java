package com.soywiz.flash.display;

import com.soywiz.flash.backend.EngineContext;
import com.soywiz.flash.backend.TouchEventType;
import com.soywiz.flash.backend.Updatable;
import com.soywiz.flash.util.Matrix;
import com.soywiz.flash.util.Point;
import com.soywiz.flash.util.Rectangle;
import com.soywiz.flash.util.SignalHandler;

import java.util.LinkedList;
import java.util.List;

abstract public class DisplayObject extends DisplayObjectBase {
    public double x = 0.0;
    public double y = 0.0;
    public double scaleX = 1.0;
    public double scaleY = 1.0;
    public double rotation = 0.0;
    public double alpha = 1.0;
    public boolean visible = true;
    public DisplayObjectContainer parent = null;
    DisplayObject prev = null;
    DisplayObject next = null;

    String name = null;
    boolean updating = true;
    double updateSpeed = 1.0;
    List<Updatable> components = new LinkedList<Updatable>();

    List<MouseUpdate> onMouseUpdate = new LinkedList<MouseUpdate>();
    List<SignalHandler<Point>> onMouseTapAny = new LinkedList<SignalHandler<Point>>();

    public boolean interactive() {
        return onMouseUpdate.size() > 0;
    }

    public <T extends Updatable> T getComponent(Class<T> clazz) {
        for (Updatable component : components) if (component.getClass() == clazz) return (T) component;
        throw (new RuntimeException("Can't find component of class '" + clazz + "'"));
    }

    /*
    def getComponent[T <: Updatable](clazz:Class[T]): T = {
        for (component <- components) if (component.getClass == clazz) return component.asInstanceOf[T]
        throw new Exception(s"Can't find component of class '$clazz'")
    }
    */

    @Override
    public Stage stage() {
        if (parent == null) return null;
        return parent.stage();
    }

    public void render(EngineContext context) {
        if (!visible) return;

        try {
            context.save();
            context.translate((float) x, (float) y);
            context.rotate((float) Math.toRadians(rotation));
            context.scale((float) scaleX, (float) scaleY);
            context.alpha((float) alpha);
            renderInternal(context);
        } finally {
            context.restore();
        }
    }

    protected void renderInternal(EngineContext context) {
    }

    private boolean lastInside = false;

    public void touchUpdate(Point point, TouchEventType kind) {
        if (onMouseTapAny.size() > 0) {
            if (kind == TouchEventType.DOWN) {
                for (SignalHandler<Point> item : onMouseTapAny) item.run(new Point(0, 0));
            }
        }

        if (interactive()) {
            Rectangle globalBounds = this.globalBounds();
            boolean inside = globalBounds.contains(point);
            Point localPoint = globalToLocal(point);

            for (MouseUpdate item : onMouseUpdate) {
                if (inside) {
                    if (kind == TouchEventType.CLICK) {
                        item.click(localPoint);
                    } else {
                        if (!lastInside) {
                            item.over(localPoint);
                        }
                        item.move(localPoint);
                    }
                } else {
                    if (lastInside) {
                        item.out(localPoint);
                    }
                }
            }

            lastInside = inside;
        }
    }

    public void update(int dt) {
        for (Updatable component : components) component.update(dt);
    }

    public Matrix transformMatrix() {
        Matrix matrix = new Matrix();
        matrix.translate((float) x, (float) y);
        matrix.rotate((float) Math.toRadians(rotation));
        matrix.scale((float) scaleX, (float) scaleY);
        return matrix;
    }


    public Matrix globalTransformMatrix() {
        return globalTransformMatrix(new Matrix());
    }

    public Matrix globalTransformMatrix(Matrix output) {
        DisplayObject node = this;
        while (node != null) {
            output.preconcat(node.transformMatrix());
            node = node.parent;
        }
        return output;
    }

  /*
  def width: Int = 0
  def height: Int = 0
  def width_= (value:Int) { }
  def height_= (value:Int) { }
  */

    final public Rectangle globalBounds() {
        return getLocalUntransformedBounds().transform(globalTransformMatrix());
    }

    final public Rectangle localBounds() {
        return getLocalUntransformedBounds().transform(transformMatrix());
    }

    public Rectangle getLocalUntransformedBounds() {
        return new Rectangle(0, 0, 0, 0);
    }

    final public Point localToGlobal(Point point) {
        return globalTransformMatrix().transformPoint(point);
    }

    final public Point globalToLocal(Point point) {
        return globalTransformMatrix().invert().transformPoint(point);
    }

}
