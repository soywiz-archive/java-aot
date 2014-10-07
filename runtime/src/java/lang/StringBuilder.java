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

    public StringBuilder append(Object v) {
        if (v == null) return append("null"); return append(v.toString());
    }

    public StringBuilder append(float v) {
        return append((int)v);
    }

    public StringBuilder append(double v) {
        return append((int)v);
    }

    public StringBuilder append(long v) {
		return append((int)v);
	}

	public StringBuilder append(int v) {
		//String set = "0123456789abcdefghijklmnopqrtuvwxyz";
        String set = "0123456789";
		char[] out = new char[128];
		int index = out.length;
		int count = 0;
		if (v == 0) return append(new String(new char[] { '0' }));
		boolean isNegative = (v < 0);
        if (isNegative) v = -v;
		while (v != 0) {
			out[--index] = set.charAt((int)(v % 10));
            //_Native.debugint(out[index]);
			count++;
			v /= 10;
		}
		if (isNegative) append("-");
        //_Native.debugint(out.length);
        //_Native.debugint(index);
        //_Native.debugint(count);
		return append(new String(out, index, count));
	}

	public String toString() {
		return new String(value);
	}
}