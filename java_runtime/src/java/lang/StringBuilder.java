package java.lang;

public class StringBuilder {
	char[] value = new char[0];

	public StringBuilder append(String str) {
		char[] newValue = new char[value.length + str.length()];
		for (int n = 0; n < value.length; n++) newValue[n] = value[n];
		for (int n = 0; n < str.length(); n++) newValue[n + value.length] = str.chars[n];

		this.value = newValue;
		return this;
	}

	public StringBuilder append(int v) {
		String chars = "0123456789";
		char[] chars2 = new char[1];
		chars2[0] = chars.charAt((v % 10));

		return append(new String(chars2));
	}

	public String toString() {
		return new String(value);
	}
}