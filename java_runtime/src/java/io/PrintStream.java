package java.io;

public class PrintStream extends FilterOutputStream {
	public PrintStream(OutputStream outputStream) {
		super(outputStream);
	}

	public void println(String s) {
		int length = s.length();
		for (int n = 0; n < length; n++) {
			outputStream.write(s.charAt(n));
		}
		outputStream.write((byte)'\n');
	}
}
