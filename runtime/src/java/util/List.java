package java.util;

public interface List<T> extends Iterable<T> {
    public int size();
    public void add(T item);
    public T[] toArray(T[] ref);
}
