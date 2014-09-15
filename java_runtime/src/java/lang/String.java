package java.lang;

public class String {
	public char[] chars;

	public String(char[] chars) {
		this.chars = chars;
	}

	public char charAt(int index) { return chars[index]; }

	public int length() { return chars.length; }
}