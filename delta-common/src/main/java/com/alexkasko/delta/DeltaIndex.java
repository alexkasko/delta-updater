package com.alexkasko.delta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * User: alexkasko
 * Date: 11/18/11
 */
class DeltaIndex {
    final ImmutableList<IndexEntry.Unchanged> unchanged;
    final ImmutableList<IndexEntry.Created> created;
    final ImmutableList<IndexEntry.Updated> updated;
    final ImmutableList<IndexEntry.Deleted> deleted;

    DeltaIndex(ImmutableList<IndexEntry.Created> created, ImmutableList<IndexEntry.Deleted> deleted,
                      ImmutableList<IndexEntry.Updated> updated, ImmutableList<IndexEntry.Unchanged> unchanged) {
        this.unchanged = unchanged;
        this.created = created;
        this.updated = updated;
        this.deleted = deleted;
    }

    Iterable<? extends IndexEntry> getAll() {
        return Iterables.concat(unchanged, created, updated, deleted);
    }
}
