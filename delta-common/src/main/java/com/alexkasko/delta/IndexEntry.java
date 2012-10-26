package com.alexkasko.delta;

import org.apache.commons.lang.builder.ToStringBuilder;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * User: alexkasko
 * Date: 11/18/11
 */
abstract class IndexEntry {
    enum State {UNCHANGED, CREATED, UPDATED, DELETED}
    final String path;
    final State state;
    final String oldSha1;
    final String newSha1;

    protected IndexEntry(String path, State state, String oldSha1, String newSha1) {
        this.path = path;
        this.state = state;
        this.oldSha1 = oldSha1;
        this.newSha1 = newSha1;
    }

    // children for type safety, easy filtering etc

    static class Created extends IndexEntry {
        public Created(String path, String oldSha1, String newSha1) {
            super(path, State.CREATED, oldSha1, newSha1);
        }
    }

    static class Deleted extends IndexEntry {
        public Deleted(String path, String oldSha1, String newSha1) {
            super(path, State.DELETED, oldSha1, newSha1);
        }
    }

    static class Updated extends IndexEntry {
        public Updated(String path,String oldSha1, String newSha1) {
            super(path, State.UPDATED, oldSha1, newSha1);
        }
    }

    static class Unchanged extends IndexEntry {
        public Unchanged(String path, String oldSha1, String newSha1) {
            super(path, State.UNCHANGED, oldSha1, newSha1);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
    }
}


