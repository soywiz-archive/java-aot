package jflash.display;

import jflash.backend.EngineContext;
import jflash.backend.TouchEventType;
import jflash.util.Point;
import jflash.util.Rectangle;

import java.util.LinkedList;
import java.util.List;

abstract public class DisplayObjectContainer extends DisplayObject {
    DisplayObject first = null;
    DisplayObject last = null;
    private int _numChildren = 0;
    //private var children:Array[DisplayObject] = new Array[DisplayObject](0)

    public int numChildren() {
        return _numChildren;
    }

    private void _addChildNode(DisplayObject child) {
        if (child == null) throw new RuntimeException("Invalid child");
        if (child == this) throw new RuntimeException("Can't add to itself");
        if (child.parent != null) child.parent.removeChild(child);

        child.parent = this;
        if (last == null) {
            last = child;
            first = child;
            child.prev = null;
            child.next = null;
        } else {
            child.next = null;
            child.prev = last;
            last.next = child;
            last = child;
        }
        _numChildren += 1;

        //if (_childsByName) _childsByName[child.name] = child
    }

    private void _removeChildNode(DisplayObject child) {
        if (child == null) throw new RuntimeException("Invalid child");
        if (child.parent == null) throw new RuntimeException("Container doesn't contain child");
        if (child.parent != this) throw new RuntimeException("Container doesn't contain child");

        //if (_childsByName) _childsByName[child.name] = undefined;

        if (child == first) first = child.next;
        if (child == last) last = child.prev;
        if (child.prev != null) child.prev.next = child.next;
        if (child.next != null) child.next.prev = child.prev;

        child.parent._numChildren -= 1;
        child.parent = null;
        child.prev = null;
        child.next = null;
    }

    public void addChild(DisplayObject child) {
        _addChildNode(child);
    }

    public void removeChild(DisplayObject child) {
        _removeChildNode(child);
    }

    public void removeChildren() {
        while (last != null) _removeChildNode(last);
    }

    protected DisplayObject transformChild(DisplayObject child) {
        return child;
    }

    public DisplayObject getChildAt(int index) {
        int currentIndex = 0;
        DisplayObject child = this.first;
        while (child != null) {
            if (currentIndex == index) return transformChild(child);
            child = child.next;
            currentIndex++;
        }
        return null;
    }

    public DisplayObject getChildByName(String name) {
        DisplayObject child = this.first;
        while (child != null) {
            if (child.name.equals(name)) return transformChild(child);
            child = child.next;
        }
        return null;
    }

    @Override
    public Rectangle getLocalUntransformedBounds() {
        DisplayObject[] children = this.children();
        Rectangle[] rectangles = new Rectangle[children.length];
        for (int n = 0; n < children.length; n++) {
            rectangles[n] = children[n].getLocalUntransformedBounds();
        }
        return Rectangle.bounds(rectangles);
    }

    @Override
    public void update(int dt) {
        super.update(dt);
        DisplayObject child = this.first;
        while (child != null) {
            if (child.updating) child.update((int) (dt * child.updateSpeed));
            child = child.next;
        }
    }

    @Override
    public void touchUpdate(Point point, TouchEventType kind) {
        super.touchUpdate(point, kind);
        DisplayObject child = this.first;
        while (child != null) {
            child.touchUpdate(point, kind);
            child = child.next;
        }
    }

    @Override
    protected void renderInternal(EngineContext context) {
        DisplayObject child = this.first;
        while (child != null) {
            child.render(context);
            child = child.next;
        }
    }

    public DisplayObject[] children() {
        List<DisplayObject> buffer = new LinkedList<DisplayObject>();

        DisplayObject child = this.first;
        while (child != null) {
            buffer.add(child);
            child = child.next;
        }

        return buffer.toArray(new DisplayObject[buffer.size()]);
    }
}
