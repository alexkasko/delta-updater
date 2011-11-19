package ru.concerteza.delta.diff;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nothome.delta.Delta;
import com.nothome.delta.GDiffWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import ru.concerteza.delta.common.IndexEntry;
import ru.concerteza.delta.common.DeltaIndex;
import ru.concerteza.delta.common.WriteOnlyOutputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static ru.concerteza.delta.common.Sha1Utils.computeSha1;

/**
 * User: alexey
 * Date: 11/18/11
 */
public class DirDeltaCreator {
    private static final String EMPTY_STRING = "";

    public void create(File oldDir, File newDir, File outFile) throws IOException {
        OutputStream out = null;
        try {
            out = FileUtils.openOutputStream(outFile);
            IOFileFilter filter = TrueFileFilter.TRUE;
            create(oldDir, newDir, filter, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void create(File oldDir, File newDir, IOFileFilter filter, OutputStream patch) throws IOException {
        DeltaIndex paths = readDeltaPaths(oldDir, newDir, filter);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(patch));
        writeIndex(paths, out);
        writeCreated(paths.getCreated(), newDir, out);
        writeUpdated(paths.getUpdated(), oldDir, newDir, out);
        out.close();
    }

    private DeltaIndex readDeltaPaths(File oldDir, File newDir, IOFileFilter filter) throws IOException {
        if(!(null != oldDir && oldDir.exists() && oldDir.isDirectory())) throw new IOException("Bad oldDir argument");
        if(!(null != newDir && newDir.exists() && newDir.isDirectory())) throw new IOException("Bad newDir argument");
        // read files
        Collection<File> oldFiles = listFiles(oldDir, filter, filter);
        Collection<File> newFiles = listFiles(newDir, filter, filter);
        // want to do comparing on strings, without touching FS
        Set<String> oldSet = ImmutableSet.copyOf(Collections2.transform(oldFiles, new Relativiser(oldDir)));
        Set<String> newSet = ImmutableSet.copyOf(Collections2.transform(newFiles, new Relativiser(newDir)));
        // partitioning
        List<String> createdPaths = Ordering.natural().immutableSortedCopy(Sets.difference(newSet, oldSet));
        List<String> existedPaths = Ordering.natural().immutableSortedCopy(Sets.intersection(oldSet, newSet));
        List<String> deletedPaths = Ordering.natural().immutableSortedCopy(Sets.difference(oldSet, newSet));
        // converting
        List<IndexEntry.Created> created = ImmutableList.copyOf(Lists.transform(createdPaths, new CreatedIndexer(newDir)));
        List<IndexEntry.Deleted> deleted = ImmutableList.copyOf(Lists.transform(deletedPaths, new DeletedIndexer(oldDir)));
        List<IndexEntry> existed = Lists.transform(existedPaths, new ExistedIndexer(oldDir, newDir));
        // partitioning
        List<IndexEntry.Updated> updated = ImmutableList.copyOf(Iterables.filter(existed, IndexEntry.Updated.class));
        List<IndexEntry.Unchanged> unchanged = ImmutableList.copyOf(Iterables.filter(existed, IndexEntry.Unchanged.class));
        return new DeltaIndex(created, deleted, updated, unchanged);
    }

    private void writeIndex(DeltaIndex paths, ZipOutputStream out) throws IOException {
        // lazy transforms
        out.putNextEntry(new ZipEntry(".index_" + UUID.randomUUID().toString()));
        Gson gson = new GsonBuilder().create();
        Writer writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        for (IndexEntry ie : paths.getAll()) {
            gson.toJson(ie, IndexEntry.class, writer);
            writer.write("\n");
        }
        writer.flush();
        out.closeEntry();
    }

    private void writeCreated(List<IndexEntry.Created> paths, File newDir, ZipOutputStream out) throws IOException {
        for(IndexEntry.Created en : paths) {
            out.putNextEntry(new ZipEntry(en.getPath()));
            File file = new File(newDir, en.getPath());
            FileUtils.copyFile(file, out);
            out.closeEntry();
        }
    }

    private void writeUpdated(List<IndexEntry.Updated> paths, File oldDir, File newDir, ZipOutputStream out) throws IOException {
        for(IndexEntry.Updated en : paths) {
            out.putNextEntry(new ZipEntry(en.getPath() + ".gdiff"));
            File source = new File(oldDir, en.getPath());
            File target = new File(newDir, en.getPath());
            computeDelta(source, target, out);
            out.closeEntry();
        }
    }

    private void computeDelta(File source, File target, OutputStream out) throws IOException {
        OutputStream guarded = WriteOnlyOutputStream.wrap(out);
        GDiffWriter writer = new GDiffWriter(guarded);
        new Delta().compute(source, target, writer);
    }

    private class Relativiser implements Function<File, String> {
        private final String parent;

        private Relativiser(File parent) {
            this.parent = separatorsToUnix(parent.getPath());
        }

        @Override
        public String apply(File input) {
            String path = separatorsToUnix(input.getPath());
            // check whether actual child
            checkArgument(parent.equals(path.substring(0, parent.length())));
            String relative = path.substring(parent.length() + 1);
            return IOCase.SYSTEM.isCaseSensitive() ? relative : relative.toLowerCase();
        }
    }

    private class CreatedIndexer implements Function<String, IndexEntry.Created> {
        private final File parent;

        private CreatedIndexer(File parent) {
            this.parent = parent;
        }

        @Override
        public IndexEntry.Created apply(String path) {
            String sha1 = computeSha1(new File(parent, path));
            return new IndexEntry.Created(path, EMPTY_STRING, sha1);
        }
    }

    private class DeletedIndexer implements Function<String, IndexEntry.Deleted> {
        private final File parent;

        private DeletedIndexer(File parent) {
            this.parent = parent;
        }

        @Override
        public IndexEntry.Deleted apply(String path) {
            String sha1 = computeSha1(new File(parent, path));
            return new IndexEntry.Deleted(path, sha1, EMPTY_STRING);
        }
    }

    private class ExistedIndexer implements Function<String, IndexEntry> {
        private final File oldParent;
        private final File newParent;

        private ExistedIndexer(File oldParent, File newParent) {
            this.oldParent = oldParent;
            this.newParent = newParent;
        }

        @Override
        public IndexEntry apply(String path) {
            String oldSha1 = computeSha1(new File(oldParent, path));
            String newSha1 = computeSha1(new File(newParent, path));
            if(oldSha1.equals(newSha1)) {
                return new IndexEntry.Unchanged(path, oldSha1, newSha1);
            } else {
                return new IndexEntry.Updated(path, oldSha1, newSha1);
            }
        }
    }
}
