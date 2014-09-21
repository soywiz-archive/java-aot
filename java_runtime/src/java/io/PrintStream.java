package java.io;

public class PrintStream extends FilterOutputStream {
    public PrintStream(OutputStream outputStream) {
        super(outputStream);
    }

    public void println(String s) {
        try {
            int length = s.length();
            for (int n = 0; n < length; n++) {
                out.write(s.charAt(n));
            }
            out.write((byte) '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
