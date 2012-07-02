package com.epeirogenic.dedupe;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public enum Checksum {

    MD5("MD5"),
    SHA1("SHA1"),
    SHA256("SHA-256");

    private final String algorithm;

    Checksum(String algorithm) {
        this.algorithm = algorithm;
    }

    private byte[] createChecksum(File file) throws Exception {
        InputStream fis =  new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance(algorithm);
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

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public String generateFor(File file) throws Exception {

        byte[] checksumBytes = createChecksum(file);
        String result = "";
        for (byte b : checksumBytes) {
            result += Integer.toString( ( b & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public static void main(String args[]) {

        Checksum checksum = Checksum.MD5;

        try {
            for(String filename : args) {
                System.out.println(filename + " : " + checksum.generateFor(new File(filename)));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}