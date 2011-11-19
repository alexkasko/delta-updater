package ru.concerteza.delta.common;

/**
 * User: alexey
 * Date: 11/18/11
 */
public class IndexEntry {
    public enum State {UNCHANGED, CREATED, UPDATED, DELETED}
    private final String path;
    private final State state;
    private final String oldSha1;
    private final String newSha1;

    protected IndexEntry(String path, State state, String oldSha1, String newSha1) {
        this.path = path;
        this.state = state;
        this.oldSha1 = oldSha1;
        this.newSha1 = newSha1;
    }

    public String getPath() {
        return path;
    }

    public String getOldSha1() {
        return oldSha1;
    }

    public String getNewSha1() {
        return newSha1;
    }

    // children for type safety, easy filtering etc

    public static class Created extends IndexEntry {
        public Created(String path, String oldSha1, String newSha1) {
            super(path, State.CREATED, oldSha1, newSha1);
        }
    }

    public static class Deleted extends IndexEntry {
        public Deleted(String path, String oldSha1, String newSha1) {
            super(path, State.DELETED, oldSha1, newSha1);
        }
    }

    public static class Updated extends IndexEntry {
        public Updated(String path,String oldSha1, String newSha1) {
            super(path, State.UPDATED, oldSha1, newSha1);
        }
    }

    public static class Unchanged extends IndexEntry {
        public Unchanged(String path, String oldSha1, String newSha1) {
            super(path, State.UNCHANGED, oldSha1, newSha1);
        }
    }
}


