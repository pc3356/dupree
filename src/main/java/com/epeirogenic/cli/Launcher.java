package com.epeirogenic.cli;

import com.epeirogenic.dedupe.Checksum;
import com.epeirogenic.dedupe.FileRecurse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Launcher {

    private final static String DEFAULT_FILENAME_FORMAT = "dupree-{0}.csv";

    public static void main(final String[] args) throws Exception {

        if(args.length == 2) {

            final File startPoint = new File(args[0]);
            if(!startPoint.exists()) {
                System.err.println("Startpoint '" + startPoint.getCanonicalPath() + "' does not seem to exist");
                usage();
            }

            final File outputFile;
            final File output = new File(args[1]);
            if(output.isDirectory()) {
                outputFile = new File(output, MessageFormat.format(DEFAULT_FILENAME_FORMAT, UUID.randomUUID()));
            } else {//if(output.isFile()) {
                outputFile = output;
            }

            final FileRecurse fileRecurse = new FileRecurse(Checksum.SHA256, new SummaryCallback());

            final Map<String, Set<File>> checksums = new HashMap<>();
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

    private static void writeResults(final Map<String, Set<File>> checksums, final File outputFile) {

        final StringBuilder output = new StringBuilder();
        for(Map.Entry<String, Set<File>> entry : checksums.entrySet()) {
            if(entry.getValue().size() > 1) {
                for (final File match : entry.getValue()) {
                    String path;
                    try {
                        path = match.getCanonicalPath();
                    } catch (IOException ioe) {
                        path = "Error getting path";
                    }
                    output.append(entry.getKey())
                            .append(',')
                            .append(path)
                            .append(',')
                            .append(match.length())
                            .append(',')
                            .append(match.lastModified()/1000)
                            .append(',')
                            .append(Instant.ofEpochSecond((match.lastModified()/1000)))
                            .append('\n');
                }
            }
        }

        try {
            Files.write(outputFile.toPath(), output.toString().getBytes());
        } catch(final IOException ioe) {
            log.error("Unable to write output");
            if (log.isDebugEnabled()) {
                ioe.printStackTrace(System.err);
            }
        }
    }

    private static int countDuplicates(final Map<String, Set<File>> checksums) {
        return (int) checksums.entrySet().stream().filter(e -> e.getValue().size() > 1).count();
    }
}

class SummaryCallback implements FileRecurse.Callback {

    // print statements are deliberate - part of UI
    @Override
    public void currentFile(final File file) {
    }

    @Override
    public void currentDirectory(final File directory) {
        printLn();
        try {
            printLn();
            System.out.print(directory.getCanonicalPath());
        } catch(final IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    private void printLn() {
        System.out.print((char) 13);
    }
}
