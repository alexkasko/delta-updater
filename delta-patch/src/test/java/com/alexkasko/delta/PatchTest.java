package com.alexkasko.delta;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static junit.framework.Assert.assertTrue;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.junit.Assert.assertEquals;
import static com.alexkasko.delta.HashUtils.computeSha1;

/**
 * User: alexkasko
 * Date: 10/26/12
 */
public class PatchTest {
    @Test
    public void test() throws IOException {
        File tmpdir = null;
        InputStream diff = null;
        try {
            tmpdir = createTmpDir();
            // prepare source dir
            File source = new File(tmpdir, "source");
            assertTrue("Cannot create tmp directory", source.mkdirs());
            File unchanged = new File(source, "unchanged");
            writeStringToFile(unchanged, "foo", "UTF-8");
            writeStringToFile(new File(source, "deleted"), "bar", "UTF-8");
            File updated = new File(source, "updated");
            writeStringToFile(updated, "baz", "UTF-8");
            File added = new File(source, "added");
            // load patch
            diff = PatchTest.class.getResourceAsStream("/diff.zip");
            // apply patch
            new DirDeltaPatcher().patch(source, new ZipInputStream(diff));
            // check results
            assertEquals("Files count fail", 3, source.listFiles().length);
            assertEquals("Unchanged fail", "0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33", computeSha1(unchanged));
            assertEquals("Updated fail", "020d4b62f2af4547cdf0c28e2fd937bfc28a3787", computeSha1(updated));
            assertEquals("Added fail", "92cfceb39d57d914ed8b14d0e37643de0797ae56", computeSha1(added));
        } finally {
            if(null != tmpdir) deleteDirectory(tmpdir);
            closeQuietly(diff);
        }
    }

    private File createTmpDir() {
        File baseDir = new File(getProperty("java.io.tmpdir"));
        String baseName = getClass().getName() + "_" + currentTimeMillis() + ".tmp";
        File tmp = new File(baseDir, baseName);
        assertTrue("Cannot create tmp directory", tmp.mkdirs());
        return tmp;
    }
}
