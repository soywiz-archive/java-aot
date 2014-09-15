package java.lang;

import libcore.Native;

public class SystemOut {
	public void println(String text) {
		print(text);
		print("\n");
	}
	public void print(String text) {
		int length = text.length();
		for (int n = 0; n < length; n++) {
			Native.putchar(text.charAt(n));
		}
	}

}
