package java.lang;

import libcore.Native;

public class Math {
    final static public double PI = 3.14159265359;
    static public float min(float a, float b) { return (a < b) ? a : b; }
    static public float max(float a, float b) { return (a > b) ? a : b; }
    static public float abs(float a) { return (a >= 0) ? a : -a; }
    static public double atan(double v) { return Native.atan(v); }
    static public double cos(double v) { return Native.cos(v); }
    static public double sin(double v) { return Native.sin(v); }

    static public double toRadians(double angdeg) {
        return angdeg * 57.2957795;
    }
}
