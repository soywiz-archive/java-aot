package java.io;

abstract public class OutputStream implements Closeable {
	public void close() {
	}

	public void flush() {
	}

	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	public void write(byte[] b, int off, int len) {
		for (int n = 0; n < len; n++) write(b[off + n]);
	}

	abstract public void write(int b);
}
