package com.alexkasko.delta;

import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Output stream transparent wrapper, {@link java.io.OutputStream#close()}
 * and {@link java.io.OutputStream#flush()} overriden as NOOP.
 * May be used to prevent rough libs from closing or flushing your streams
 *
 * @author alexkasko
 *         Date: 11/19/11
 */
public class NoCloseOutputStream extends OutputStream {
    private final OutputStream target;

    /**
     * @param target target stream
     */
    public NoCloseOutputStream(OutputStream target) {
        checkNotNull(target, "Provided output stream is null");
        this.target = target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        target.write(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        target.write(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        // this line intentionally left blank
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // this line intentionally left blank
    }
}