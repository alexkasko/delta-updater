package ru.concerteza.util.delta;

import java.io.IOException;
import java.io.OutputStream;

/**
 * User: alexey
 * Date: 11/19/11
 */

// make rough libs not to close or flush my streams
public class WriteOnlyOutputStream extends OutputStream {
    private final OutputStream target;

    private WriteOnlyOutputStream(OutputStream target) {
        this.target = target;
    }

    public static WriteOnlyOutputStream wrap(OutputStream target) {
        return new WriteOnlyOutputStream(target);
    }

    @Override
    public void write(int b) throws IOException {
        target.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        target.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        // this line is intentionally left blank
    }

    @Override
    public void close() throws IOException {
        // this line is intentionally left blank
    }
}
