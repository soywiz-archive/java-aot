package jflash.backend;

import jflash.util.Point;

abstract public class Component implements Renderizable, Updatable {
    abstract public void touchUpdate(Point point, TouchEventType kind);
}
