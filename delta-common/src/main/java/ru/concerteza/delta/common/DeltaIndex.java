package ru.concerteza.delta.common;

import com.google.common.collect.Iterables;

import java.util.List;

/**
 * User: alexey
 * Date: 11/18/11
 */
public class DeltaIndex {
    private final List<IndexEntry.Unchanged> unchanged;
    private final List<IndexEntry.Created> created;
    private final List<IndexEntry.Updated> updated;
    private final List<IndexEntry.Deleted> deleted;

    public DeltaIndex(List<IndexEntry.Created> created, List<IndexEntry.Deleted> deleted, List<IndexEntry.Updated> updated, List<IndexEntry.Unchanged> unchanged) {
        this.unchanged = unchanged;
        this.created = created;
        this.updated = updated;
        this.deleted = deleted;
    }

    public List<IndexEntry.Unchanged> getUnchanged() {
        return unchanged;
    }

    public List<IndexEntry.Created> getCreated() {
        return created;
    }

    public List<IndexEntry.Updated> getUpdated() {
        return updated;
    }

    public List<IndexEntry.Deleted> getDeleted() {
        return deleted;
    }

    public Iterable<? extends IndexEntry> getAll() {
        return Iterables.concat(unchanged, created, updated, deleted);
    }
}
