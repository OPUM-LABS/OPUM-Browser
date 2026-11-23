package com.mycompany.app.main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A buffered stream whose {@link #available()} reports the known total length (e.g. a file's size), taken over from
 * Universal Image Loader for {@code MainUtil.urlInputStream}.
 */
public final class ContentLengthInputStream extends InputStream {
    private final BufferedInputStream in;
    private final int length;

    public ContentLengthInputStream(BufferedInputStream in, int length) {
        this.in = in;
        this.length = length;
    }

    @Override public int available() { return length; }
    @Override public void close() throws IOException { in.close(); }
    @Override public void mark(int limit) { in.mark(limit); }
    @Override public boolean markSupported() { return in.markSupported(); }
    @Override public int read() throws IOException { return in.read(); }
    @Override public void reset() throws IOException { in.reset(); }
    @Override public long skip(long n) throws IOException { return in.skip(n); }
    @Override public int read(byte[] b) throws IOException { return in.read(b); }
    @Override public int read(byte[] b, int off, int len) throws IOException { return in.read(b, off, len); }
}
