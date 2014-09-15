package libcore;

public class Native {
	static public native char lower(char v);
	static public native char upper(char v);
	static public native void putchar(char v);
	static public native void exit(int status);
	static public native void arraycopy(Object src, int srcOfs, Object dest, int destOfs, int len);
	static public native void gc();
	static public native long currentTimeMillis();

}
