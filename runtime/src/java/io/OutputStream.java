package java.io;

abstract public class OutputStream implements Closeable, AutoCloseable {
	public void close() throws IOException {
	}

	public void flush() throws IOException {
	}

	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		for (int n = 0; n < len; n++) write(b[off + n]);
	}

	abstract public void write(int b) throws IOException;
}
