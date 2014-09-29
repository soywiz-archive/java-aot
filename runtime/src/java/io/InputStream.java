package java.io;

public abstract class InputStream implements Closeable, AutoCloseable {
    public abstract int read() throws IOException;
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    public int read(byte[] b, int off, int len) throws IOException {
        for (int n = 0; n < len; n++) {
            int v = read();
            if (v < 0) return n;
            b[off + n] = (byte)v;
        }
        return b.length;
    }

    public long skip(long n) throws IOException {
        throw(new RuntimeException());
    }

    public int available() throws IOException {
        throw(new RuntimeException());
    }

    public void close() throws IOException {
    }
}
