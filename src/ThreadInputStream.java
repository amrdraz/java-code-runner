
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

/** A ThreadInputStream replaces the normal System.in and ensures
 * that input to System.in goes to a different InputStream for
 * each thread.  It does this by using ThreadLocal to maintain a
 * InputStream for each thread. 
 * from http://maiaco.com/articles/java/threadOut.php
 */
public class ThreadInputStream extends InputStream {

    /** Changes System.in to a ThreadInputStream which will
     * send input to a separate stream for each thread. */
    public static void replaceSystemIn() {

        // Save the existing System.in
        InputStream console = System.in;

        // Create a ThreadInputStream and install it as System.in
        ThreadInputStream threadIn = new ThreadInputStream();
        System.setIn(threadIn);

        // Use the original System.in as the current thread's System.in
        threadIn.setThreadIn(console);
    }

    /** Thread specific storage to hold a InputStream for each thread */
    private ThreadLocal<InputStream> in;

    private ThreadInputStream() {
        super();
        in = new ThreadLocal<InputStream>();
    }

    /** Sets the InputStream for the currently executing thread. */
    public void setThreadIn(InputStream in) {
        this.in.set(in);
    }

    /** Returns the InputStream for the currently executing thread. */
    public InputStream getThreadIn() {
        return this.in.get();
    }

    @Override
    public int available() throws IOException {
        return getThreadIn().available();
    }
    @Override
    public void mark(int readlimit) {
        getThreadIn().mark(readlimit);
    }
    @Override
    public boolean markSupported() {
        return getThreadIn().markSupported();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return getThreadIn().read(buf, off, len);
    }
    @Override
    public int read(byte[] b) throws IOException { return getThreadIn().read(b); }
    @Override
    public int read() throws IOException { return getThreadIn().read(); }
    @Override
    public long skip(long n) throws IOException { return getThreadIn().skip(n); }
    @Override
    public void reset() throws IOException { getThreadIn().reset(); }
    @Override
    public void close() throws IOException { getThreadIn().close(); }
}
