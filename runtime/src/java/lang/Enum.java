package java.lang;

public class Enum<T> {
    private String _name;
    private int _ordinal;

    protected Enum(String name, int ordinal) {
        this._name = name;
        this._ordinal = ordinal;
    }

    public String name() { return _name; }
    public int ordinal() { return _ordinal; }

    public static <T extends Enum<T>>
    T	valueOf(Class<T> enumType, String name) {
        return null;
    }
}
