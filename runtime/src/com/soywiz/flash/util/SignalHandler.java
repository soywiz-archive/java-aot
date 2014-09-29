package com.soywiz.flash.util;

public interface SignalHandler<T> {
    void run(T value);
}
