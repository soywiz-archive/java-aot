package java.lang;

public class StringBuilder {
	char[] value = new char[0];

	public StringBuilder append(String str) {
		char[] newValue = new char[value.length + str.length()];
		for (int n = 0; n < value.length; n++) newValue[n] = value[n];
		for (int n = 0; n < str.length(); n++) newValue[n + value.length] = str.charAt(n);

		this.value = newValue;
		return this;
	}

	public StringBuilder append(long v) {
		return append((int)v);
	}

	public StringBuilder append(int v) {
		String set = "0123456789abcdefghijklmnopqrtuvwxyz";
		char[] out = new char[64];
		int index = out.length;
		int count = 0;
		if (v == 0) return append(new String(new char[] { '0' }));
		boolean isNegative = (v < 0);
		while (v != 0) {
			out[--index] = set.charAt((int)(v % 10));
			count++;
			v /= 10;
		}
		if (isNegative) append("-");
		return append(new String(out, index, count));
	}

	public String toString() {
		return new String(value);
	}
}