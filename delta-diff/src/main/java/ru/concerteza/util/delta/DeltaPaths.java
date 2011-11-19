package ru.concerteza.util.delta;

import com.google.common.collect.Iterables;

import java.util.List;

/**
 * User: alexey
 * Date: 11/18/11
 */
class DeltaPaths {
    private final List<DiffEntry.Unchanged> unchanged;
    private final List<DiffEntry.Created> created;
    private final List<DiffEntry.Updated> updated;
    private final List<DiffEntry.Deleted> deleted;

    DeltaPaths(List<DiffEntry.Created> created, List<DiffEntry.Deleted> deleted, List<DiffEntry.Updated> updated, List<DiffEntry.Unchanged> unchanged) {
        this.unchanged = unchanged;
        this.created = created;
        this.updated = updated;
        this.deleted = deleted;
    }

    public List<DiffEntry.Unchanged> getUnchanged() {
        return unchanged;
    }

    public List<DiffEntry.Created> getCreated() {
        return created;
    }

    public List<DiffEntry.Updated> getUpdated() {
        return updated;
    }

    public List<DiffEntry.Deleted> getDeleted() {
        return deleted;
    }

    public Iterable<? extends DiffEntry> getAll() {
        return Iterables.concat(unchanged, created, updated, deleted);
    }
}
