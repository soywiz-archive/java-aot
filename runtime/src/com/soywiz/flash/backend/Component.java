package com.soywiz.flash.backend;

import com.soywiz.flash.util.Point;

abstract public class Component implements Renderizable, Updatable {
    abstract public void touchUpdate(Point point, TouchEventType kind);
}
