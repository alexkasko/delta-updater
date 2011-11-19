package ru.concerteza.util.delta;

/**
 * User: alexey
 * Date: 11/18/11
 */
abstract class DiffEntry {
    private enum State {UNCHANGED, CREATED, UPDATED, DELETED}
    private final String path;
    private final State state;
    private final String oldSha1;
    private final String newSha1;

    DiffEntry(String path, State state, String oldSha1, String newSha1) {
        this.path = path;
        this.state = state;
        this.oldSha1 = oldSha1;
        this.newSha1 = newSha1;
    }

    public String getPath() {
        return path;
    }

    // children for type safety

    static class Created extends DiffEntry {
        Created(String path, String oldSha1, String newSha1) {
            super(path, State.CREATED, oldSha1, newSha1);
        }
    }

    static class Deleted extends DiffEntry {
        Deleted(String path, String oldSha1, String newSha1) {
            super(path, State.DELETED, oldSha1, newSha1);
        }
    }

    static class Updated extends DiffEntry {
        Updated(String path,String oldSha1, String newSha1) {
            super(path, State.UPDATED, oldSha1, newSha1);
        }
    }

    static class Unchanged extends DiffEntry {
        Unchanged(String path, String oldSha1, String newSha1) {
            super(path, State.UNCHANGED, oldSha1, newSha1);
        }
    }
}


