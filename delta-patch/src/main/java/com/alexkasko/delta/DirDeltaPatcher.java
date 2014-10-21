package com.alexkasko.delta;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nothome.delta.GDiffPatcher;
import com.nothome.delta.RandomAccessFileSeekableSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.UnhandledException;

import java.io.*;
import java.lang.reflect.Type;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.alexkasko.delta.HashUtils.hex;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static com.alexkasko.delta.IndexEntry.State.*;
import static com.alexkasko.delta.HashUtils.computeSha1;

/**
 * Patches directory with provided ZIP file or stream.
 * Patches are applied in fail-fast mode, application will be aborted on first wrong hash-sum or IO error.
 * <ul>
 *  <li>takes directory to patch and patch file (or stream)</li>
 *  <li>reads '.index' file and using it for futher steps:</li>
 *  <li>checks hash sums for 'unchanged' files</li>
 *  <li>reads from stream 'added' files, puts them into directory checking hash sums</li>
 *  <li>check hash sums for 'updated' files</li>
 *  <li>reads '.gdiff' patches from stream, applies them, checks hash sums for applied files</li>
 *  <li>checks hash sums for 'deleted' files</li>
 *  <li>deletes 'deleted' files</li>
 </ul>
 *
 * @author alexkasko
 * Date: 11/19/11
 */
public class DirDeltaPatcher {

    /**
     * Applies patch file to directory
     *
     * @param dir target directory
     * @param patch ZIP patch file
     * @throws IOException on any io or consistency problem
     */
    public void patch(File dir, File patch) throws IOException {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(openInputStream(patch));
            patch(dir, zis);
        } finally {
            IOUtils.closeQuietly(zis);
        }
    }

    /**
     * Applies patch stream to directory
     *
     * @param dir target directory
     * @param patch ZIP patch stream
     * @throws IOException on any io or consistency problem
     */
    public void patch(File dir, ZipInputStream patch) throws IOException {
        DeltaIndex index = readIndex(patch);
        check(index.unchanged, dir);
        create(index.created, dir, patch);
        update(index.updated, dir, patch);
        delete(index.deleted, dir);
    }

    private DeltaIndex readIndex(ZipInputStream patch) throws IOException {
        ZipEntry indexEntry = patch.getNextEntry();
        checkArgument(indexEntry.getName().startsWith(".index"), "Unexpected index file name: '{}', must start with '.index'");
        LineIterator iter = IOUtils.lineIterator(patch, "UTF-8");
        List<? extends IndexEntry> entries = ImmutableList.copyOf(Iterators.transform(iter, new IndexEntryMapper()));
        patch.closeEntry();
        ImmutableList<IndexEntry.Unchanged> unchanged = ImmutableList.copyOf(Iterables.filter(entries, IndexEntry.Unchanged.class));
        ImmutableList<IndexEntry.Created> created = ImmutableList.copyOf(Iterables.filter(entries, IndexEntry.Created.class));
        ImmutableList<IndexEntry.Updated> updated = ImmutableList.copyOf(Iterables.filter(entries, IndexEntry.Updated.class));
        ImmutableList<IndexEntry.Deleted> deleted = ImmutableList.copyOf(Iterables.filter(entries, IndexEntry.Deleted.class));
        return new DeltaIndex(created, deleted, updated, unchanged);
    }

    private void check(List<IndexEntry.Unchanged> index, File dir) throws IOException {
        for (IndexEntry.Unchanged en : index) {
            File file = new File(dir, en.path);
            if(!(file.exists() && file.isFile())) throw new FileNotFoundException(file.toString());
            String sha1 = computeSha1(file);
            if(!sha1.equals(en.oldSha1)) throw new IOException("UNCHANGED file check failed for file: " + file);
        }
    }

    private void create(List<IndexEntry.Created> index, File dir, ZipInputStream patch) throws IOException {
        for (IndexEntry.Created en : index) {
            ZipEntry entry = patch.getNextEntry();
            checkState(en.path.equals(entry.getName()), "Index and zipstream unsynchronized, index: " + en.path + ", zipstream: " + entry.getName());
            File file = new File(dir, en.path);
            if (file.exists()) throw new IOException("CREATED file already exists: " + file);
            String sha1 = copyStreamToFileWithDigest(patch, file);
            if (!sha1.equals(en.newSha1)) throw new IOException("CREATED file check failed for file: " + file);
            patch.closeEntry();
        }
    }

    private void update(List<IndexEntry.Updated> index, File dir, ZipInputStream patch) throws IOException {
        for (IndexEntry.Updated en : index) {
            ZipEntry entry = patch.getNextEntry();
            checkState((en.path + ".gdiff").equals(entry.getName()), "Index and zipstream unsynchronized, index: " + en.path + ", zipstream: " + entry.getName());
            File file = new File(dir, en.path);
            if (!file.exists()) throw new IOException("UPDATED file doesn't exist: " + file);
            String sha1old = computeSha1(file);
            if(!sha1old.equals(en.oldSha1)) throw new IOException("UPDATED file check failed for old file: " + file);
            File patched = new File(dir, en.path + UUID.randomUUID().toString());
            patch(file, patch, patched);
            String sha1new = computeSha1(patched);
            if(!sha1new.equals(en.newSha1)) throw new IOException("UPDATED file check failed for new file: " + file);
            boolean deleted = file.delete();
            checkState(deleted, "Delete file unsuccessful: " + file);
            boolean renamed = patched.renameTo(file);
            checkState(renamed, "Rename to file unsuccessful: " + file);
            patch.closeEntry();
        }
    }

    private void delete(List<IndexEntry.Deleted> index, File dir) throws IOException {
        for (IndexEntry.Deleted en : index) {
            File file = new File(dir, en.path);
            if (!file.exists()) throw new IOException("DELETED file doesn't exist: " + file);
            String sha1old = computeSha1(file);
            if(!sha1old.equals(en.oldSha1)) throw new IOException("DELETED file check failed old file: " + file);
            boolean deleted = file.delete();
            checkState(deleted, "Cannot delete file: " + file);
        }
    }

    private String copyStreamToFileWithDigest(InputStream stream, File file) throws IOException {
        DigestOutputStream output = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            OutputStream outputStream = new BufferedOutputStream(openOutputStream(file));
            output = new DigestOutputStream(outputStream, md);
            IOUtils.copyLarge(stream, output);
            output.flush();
            byte[] bytes = output.getMessageDigest().digest();
            return hex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new UnhandledException(e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    private void patch(File file, InputStream patch, File patched) throws IOException {
        OutputStream out = null;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            RandomAccessFileSeekableSource source = new RandomAccessFileSeekableSource(raf);
            out = new BufferedOutputStream(new FileOutputStream(patched));
            new GDiffPatcher().patch(source, patch, out);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(raf);
        }
    }

    private class IndexEntryMapper implements Function<String, IndexEntry> {
        private final Gson gson = new Gson();

        @Override
        public IndexEntry apply(String input) {
            Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
            Map<String, String> map = gson.fromJson(input, mapType);
            String state = map.get("state");
            String path = map.get("path");
            String oldSha1 = map.get("oldSha1");
            String newSha1 = map.get("newSha1");
            if(UNCHANGED.name().equals(state)) return new IndexEntry.Unchanged(path, oldSha1, newSha1);
            if(CREATED.name().equals(state)) return new IndexEntry.Created(path, oldSha1, newSha1);
            if(UPDATED.name().equals(state)) return new IndexEntry.Updated(path, oldSha1, newSha1);
            if(DELETED.name().equals(state)) return new IndexEntry.Deleted(path, oldSha1, newSha1);
            throw new IllegalStateException("Cannot parse index entry from line: " + input);
        }
    }
}
