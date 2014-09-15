package java.lang;

public class String {
	private char[] chars;
	private int offset;
	private int len;

	public String(char[] chars, int offset, int length) {
		this.chars = chars;
		this.offset = offset;
		this.len = length;
	}
	public String(char[] chars) {
		this.chars = chars;
		this.offset = 0;
		this.len = chars.length;
	}

	public char charAt(int index) { return chars[offset + index]; }

	public int length() { return len; }
}