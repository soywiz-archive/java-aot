package java.io;

public class FilterOutputStream extends OutputStream {
	protected OutputStream outputStream;

	public FilterOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@java.lang.Override
	public void close() {
		outputStream.close();
	}

	@java.lang.Override
	public void flush() {
		outputStream.flush();
	}

	@java.lang.Override
	public void write(byte[] b) {
		outputStream.write(b);
	}

	@java.lang.Override
	public void write(byte[] b, int off, int len) {
		outputStream.write(b, off, len);
	}

	@java.lang.Override
	public void write(int b) {
		outputStream.write(b);
	}
}

