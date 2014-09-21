package java.io;

public class FilterOutputStream extends OutputStream {
    protected OutputStream out;

    public FilterOutputStream(OutputStream out) {
        this.out = out;
    }

    @java.lang.Override
    public void close() throws IOException {
        out.close();
    }

    @java.lang.Override
    public void flush() throws IOException {
        out.flush();
    }

    @java.lang.Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @java.lang.Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @java.lang.Override
    public void write(int b) throws IOException {
        out.write(b);
    }
}

