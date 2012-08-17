package com.epeirogenic.cli;

import com.epeirogenic.dedupe.Checksum;
import com.epeirogenic.dedupe.FileRecurse;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Launcher {

    private final static String DEFAULT_FILENAME_FORMAT = "dupree-{0}.csv";

    public static void main(String[] args) throws Exception {

        if(args.length == 2) {

            File startPoint = new File(args[0]);
            if(!startPoint.exists()) {
                System.err.println("Startpoint '" + startPoint.getCanonicalPath() + "' does not seem to exist");
                usage();
            }

            File outputFile;
            File output = new File(args[1]);
            if(output.isDirectory()) {
                outputFile = new File(output, MessageFormat.format(DEFAULT_FILENAME_FORMAT, UUID.randomUUID()));
            } else {//if(output.isFile()) {
                outputFile = output;
            }

            FileRecurse fileRecurse = new FileRecurse(Checksum.SHA256, new SummaryCallback());

            Map<String, Set<File>> checksums = new HashMap<String, Set<File>>();
            fileRecurse.iterate(startPoint, checksums);
            System.out.print((char)13);
            int duplicates = countDuplicates(checksums);
            System.out.println("Found " + duplicates + " duplicates");
            if(duplicates > 0) {
                writeResults(checksums, outputFile);
            } else {
                System.out.println("No file written");
            }
            System.out.println("Done");

        } else {
            usage();
        }

    }

    private static void usage() {
        System.err.println("Usage: cmd <start> <outputFile>");
        System.err.println("If outputFile is a directory, a file 'dupree-<guid>.csv' will be created there");
    }

    private static void writeResults(Map<String, Set<File>> checksums, File outputFile) {

        StringBuilder output = new StringBuilder();
        for(Map.Entry<String, Set<File>> entry : checksums.entrySet()) {
            if(entry.getValue().size() > 1) {
                for(File match : entry.getValue()) {
                    String path;
                    try {
                        path = match.getCanonicalPath();
                    } catch(IOException ioe) {
                        path = "Error getting path";
                    }
                    output.append(entry.getKey())
                            .append(',')
                            .append(path)
                            .append(',')
                            .append(match.length())
                            .append('\n');
                }
            }
        }

        try {
            FileUtils.writeStringToFile(outputFile, output.toString());
        } catch(IOException ioe) {
            System.err.println("Unable to write output");
            ioe.printStackTrace(System.err);
        }
    }

    private static int countDuplicates(Map<String, Set<File>> checksums) {
        int c = 0;
        for(Map.Entry<String, Set<File>> entry : checksums.entrySet()) {
            if(entry.getValue().size() > 1) c++;
        }
        return c;
    }
}

class SummaryCallback implements FileRecurse.Callback {

    String currentOutput = "";

    @Override
    public void currentFile(File file) {
//        System.out.print((char)13);
//        for(int i = 0; i < currentOutput.length(); i++) {
//            System.out.print(' ');
//        }
//        try {
//            currentOutput = file.getCanonicalPath();
//            System.out.print((char)13);
//            System.out.print(currentOutput);
//        } catch(IOException ioe) {
//            ioe.printStackTrace(System.err);
//        }
    }

    @Override
    public void currentDirectory(File directory) {
        System.out.print((char) 13);
        for(int i = 0; i < currentOutput.length(); i++) {
            System.out.print(' ');
        }
        try {
            currentOutput = directory.getCanonicalPath();
            System.out.print((char)13);
            System.out.print(currentOutput);
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
}
