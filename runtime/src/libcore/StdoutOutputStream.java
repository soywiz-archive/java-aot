package libcore;

import java.io.OutputStream;

public class StdoutOutputStream extends OutputStream {
	public StdoutOutputStream() {
	}

	@Override
	public void write(int b) {
		Native.putchar((char)b);
	}
}