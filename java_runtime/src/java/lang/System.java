package java.lang;

import libcore.Native;
import libcore.StdoutOutputStream;
import libcore.SystemOut;

import java.io.PrintStream;

public class System {
	//public static final PrintStream err = new PrintStream(null);
	//public static final PrintStream out = new PrintStream(new StdoutOutputStream());

	public static final SystemOut out = new SystemOut();


	public static void arraycopy(Object src, int srcOfs, Object dest, int destOfs, int len) {
		Native.arraycopy(src, srcOfs, dest, destOfs, len);
	}

	public static long currentTimeMillis() {
		return Native.currentTimeMillis();
	}

	public static void gc() {
		Native.gc();
	}

	public static void exit(int status) {
		Native.exit(status);
	}

	public static String getProperty(String key, String def) {
		if (key.equals("file.separator")) return "/";
		if (key.equals("path.separator")) return ":";
		if (key.equals("line.separator")) return lineSeparator();
		return def;
	}

	public static String lineSeparator() {
		return "\n";
	}

	public static int identityHashCode(Object o) {
		if (o == null) return 0;
		return o.hashCode();
	}

    /*
	public static void setErr(PrintStream err) {
	}

	public static void setOut(PrintStream out) {
	}
	*/
}
