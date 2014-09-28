package java.io;

public class PrintStream extends FilterOutputStream {
    public PrintStream(OutputStream outputStream) {
        super(outputStream);
    }

    public void print(String s) {
        try {
            int length = s.length();
            for (int n = 0; n < length; n++) {
                out.write(s.charAt(n));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void println(String s) {
        print(s);
        print("\n");
    }
}
