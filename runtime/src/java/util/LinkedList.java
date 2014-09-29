package java.util;

public class LinkedList<T> implements List<T> {
    private Object[] items = new Object[0];

    public LinkedList() {
    }

    @Override
    public int size() {
        return items.length;
    }

    public void add(T item) {
        Object[] newItems = new Object[items.length + 1];
        for (int n = 0; n < items.length; n++) newItems[n] = items[n];
        newItems[items.length] = item;
        this.items = newItems;
    }

    @Override
    public T[] toArray(T[] ref) {
        for (int n = 0; n < ref.length; n++) ref[n] = (T)items[n];
        return ref;
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIterator();
    }

    class ListIterator<T> implements Iterator<T> {
        private int offset = 0;

        @Override
        public boolean hasNext() {
            return offset < items.length;
        }

        @Override
        public T next() {
            return (T)items[offset];
        }

        @Override
        public void remove() {

        }
    }
}

