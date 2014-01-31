package com.alexkasko.delta;

import java.io.OutputStream;

/**
 * alexkasko: Borrowed from Guava, was removed in recent Guava versions.
 * Implementation of {@link java.io.OutputStream} that simply discards written bytes.
 *
 * @author Spencer Kimball
 * @since 1.0
 */
public final class NullOutputStream extends OutputStream {
  /** Discards the specified byte. */
  @Override public void write(int b) {
  }

  /** Discards the specified byte array. */
  @Override public void write(byte[] b, int off, int len) {
  }
}
