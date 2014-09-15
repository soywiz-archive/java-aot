package java.lang;

import libcore.Native;

public class String {
	private char[] chars;
	private int offset;
	private int len;

	public String() {
		this(new char[0]);
	}

	public String(char[] chars) {
		this(chars, 0, chars.length);
	}

	public String(char[] chars, int offset, int length) {
		this.chars = chars;
		this.offset = offset;
		this.len = length;
	}

	public String(String original) {
		this.chars = original.chars;
		this.offset = original.offset;
		this.len = original.len;
	}

	public char charAt(int index) {
		return chars[offset + index];
	}

	public String toUpperCase() {
		char[] out = new char[len];
		for (int n = 0; n < len; n++) out[n] = Native.upper(charAt(n));
		return new String(out);
	}

	public String toLowerCase() {
		char[] out = new char[len];
		for (int n = 0; n < len; n++) out[n] = Native.lower(charAt(n));
		return new String(out);
	}

	public int length() { return len; }
}