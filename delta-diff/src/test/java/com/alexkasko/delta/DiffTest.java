package com.alexkasko.delta;

import com.alexkasko.delta.DirDeltaCreator;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.alexkasko.delta.HashUtils.hex;
import static java.lang.System.currentTimeMillis;
import static junit.framework.Assert.assertTrue;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;
import static org.junit.Assert.assertEquals;

/**
 * User: alexkasko
 * Date: 10/26/12
 */
public class DiffTest {
    @Test
    public void test() throws IOException, NoSuchProviderException, NoSuchAlgorithmException {
        File tmpdir = null;
        try {
            tmpdir = createTmpDir();
            // prepare source dir
            File source = new File(tmpdir, "source");
            assertTrue("Cannot create tmp directory", source.mkdirs());
            writeStringToFile(new File(source, "unchanged"), "foo", "UTF-8");
            writeStringToFile(new File(source, "deleted"), "bar", "UTF-8");
            writeStringToFile(new File(source, "updated"), "baz", "UTF-8");
            // prepare target dir
            File target = new File(tmpdir, "target");
            writeStringToFile(new File(target, "added"), "42", "UTF-8");
            writeStringToFile(new File(target, "unchanged"), "foo", "UTF-8");
            writeStringToFile(new File(target, "updated"), "ba42", "UTF-8");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // create diff
            new DirDeltaCreator().create(source, target, TRUE, baos);
            // read diff as zip
            byte[] delta = baos.toByteArray();
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(delta));
            // check contents
            ZipEntry indexEntry = zis.getNextEntry();
            assertTrue("Index name fail", indexEntry.getName().startsWith(".index_"));
            assertEquals("Index body fail", "4cd7b00b116611d04981d72d07b056f05bbf424c", hash(zis));
            zis.closeEntry();
            ZipEntry addEntry = zis.getNextEntry();
            assertEquals("Added name fail", "added", addEntry.getName());
            assertEquals("Added body fail", "92cfceb39d57d914ed8b14d0e37643de0797ae56", hash(zis));
            zis.closeEntry();
            ZipEntry updatedEntry = zis.getNextEntry();
            assertEquals("Updated name fail", "updated.gdiff", updatedEntry.getName());
            assertEquals("Updated body fail", "1881f0e90abaa44fd54f35b95ad3ef8ebc44ffd0", hash(zis));
            zis.closeEntry();
            zis.close();
        } finally {
            if(null != tmpdir) deleteDirectory(tmpdir);
        }
    }

    private File createTmpDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = getClass().getName() + "_" + currentTimeMillis() + ".tmp";
        File tmp = new File(baseDir, baseName);
        assertTrue("Cannot create tmp directory", tmp.mkdirs());
        return tmp;
    }

    private String hash(InputStream is) throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1", "SUN");
        byte[] digest = sha1.digest(baos.toByteArray());
        return hex(digest);
    }
}
