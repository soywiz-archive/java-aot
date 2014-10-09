package libcore;

@CPPClass(header =
    "#include <wchar.h>\n" +
    "#include <sys/time.h>\n" +
    "#include <stdio.h>\n" +
    "#include <math.h>\n" +
    "#include <stdlib.h>\n"
)
public class Native {
    static public native char lower(char v);
	static public native char upper(char v);
    static public native double atan(double v);
    static public native double cos(double v);
    static public native double sin(double v);
    static public native float intBitsToFloat(int v);
    static public native boolean isNaN(float v);
    static public native void putchar(char v);
    static public native void flush();
    static public native void debugint(int v);
    static public native void exit(int status);
	static public native void arraycopy(Object src, int srcOfs, Object dest, int destOfs, int len);
	static public native void gc();
	static public native long currentTimeMillis();
    static public native int getTimerTime();
}
