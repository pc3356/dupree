package com.epeirogenic.dedupe;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * http://www.rgagnon.com/javadetails/java-0416.html
 */
public enum Checksum {

    MD5("MD5"),
    SHA1("SHA1"),
    SHA256("SHA-256");

    private final String algorithm;

    Checksum(final String algorithm) {
        this.algorithm = algorithm;
    }

    private byte[] createChecksum(File file) throws Exception {
        InputStream fis =  new FileInputStream(file);

        byte[] buffer = new byte[1024];
        final MessageDigest complete = MessageDigest.getInstance(algorithm);
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    public String generateFor(final File file) throws Exception {

        byte[] checksumBytes = createChecksum(file);
        return getHex(checksumBytes);
    }

    public static void main(final String args[]) {

        final Checksum checksum = Checksum.MD5;

        try {
            for(final String filename : args) {
                System.out.println(filename + " : " + checksum.generateFor(new File(filename)));
            }
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static final String HEXES = "0123456789ABCDEF";

    private String getHex(final byte[] checksumBytes) {
        if ( checksumBytes == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * checksumBytes.length );
        for ( final byte b : checksumBytes ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    private String getHexString(final byte[] checksumBytes) {
        String result = "";
        for (final byte b : checksumBytes) {
            result += Integer.toString( ( b & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
}