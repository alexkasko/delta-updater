package com.alexkasko.delta;

import com.google.common.io.NullOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.UnhandledException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: alexkasko
 * Date: 11/19/11
 */
class HashUtils {
    static String computeSha1(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            DigestInputStream dis = new DigestInputStream(is, md);
            IOUtils.copyLarge(dis, new NullOutputStream());
            dis.close();
            byte[] bytes = dis.getMessageDigest().digest();
            return hex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new UnhandledException(e);
        } catch (IOException e) {
            throw new UnhandledException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    //  http://stackoverflow.com/a/3940857/314015
    static String hex(byte[] data) {
        return String.format("%040x", new BigInteger(1, data));
    }
}
