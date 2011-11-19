package ru.concerteza.delta.common;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkState;

/**
 * User: alexey
 * Date: 11/19/11
 */
public abstract class SingleUseIterable<T> implements Iterable<T> {
    private boolean notUsed = true;

    @Override
    public Iterator<T> iterator() {
        checkState(notUsed, "SingleUseIterable is already used: %s", this);
        notUsed = false;
        return singleUseIterator();
    }

    protected abstract Iterator<T> singleUseIterator();

    public static <T> Iterable<T> wrap(Iterator<T> iter) {
        return new Wrapper<T>(iter);
    }

    private static class Wrapper<T> extends SingleUseIterable<T> {
        private final Iterator<T> iter;

        private Wrapper(Iterator<T> iter) {
            this.iter = iter;
        }

        @Override
        protected Iterator<T> singleUseIterator() {
            return iter;
        }
    }
}