package java.lang;

import libcore.Native;

public class Float extends Number {
    final static public float MIN_VALUE = Float.intBitsToFloat(0x1);
    final static public float MAX_VALUE = Float.intBitsToFloat(0x7f7fffff);

    static public boolean isNaN(float value) { return Native.isNaN(value); }

    static public float intBitsToFloat(int value) { return Native.intBitsToFloat(value); }
}
