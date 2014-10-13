package jflash.util;

import java.util.LinkedList;
import java.util.List;

public class Signal<T> {
    List<SignalHandler<T>> handlers = new LinkedList<SignalHandler<T>>();

    public void add(SignalHandler<T> handler) {
        handlers.add(handler);
    }

    public void apply(T value) {
        for (SignalHandler<T> handler : handlers) {
            handler.run(value);
        }
    }

}
