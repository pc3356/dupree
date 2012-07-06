package com.epeirogenic.dedupe;

import org.apache.commons.io.filefilter.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class FileRecurse {

    private final static Logger LOGGER = Logger.getLogger(FileRecurse.class);

    private final Checksum checksum;
    private final Callback callback;
    private boolean ignoreHiddenDirectories = true;
    private boolean ignoreHiddenFiles = true;

    public FileRecurse(Checksum checksum, Callback callback) {
        this.checksum = checksum;
        this.callback = callback;
    }

    public void setIgnoreHiddenDirectories(boolean ignore) {
        ignoreHiddenDirectories = ignore;
    }

    public void setIgnoreHiddenFiles(boolean ignore) {
        ignoreHiddenFiles = ignore;
    }

    private void iterateOverFiles(File[] files, Map<String, Set<File>> checksumMap) {
        for(File file : files) {
            callback.currentFile(file);
            try {
                String checksumString = checksum.generateFor(file);
                Set<File> fileSet = findMatchingChecksum(checksumMap, file, checksumString);
                fileSet.add(file);
                checksumMap.put(checksumString, fileSet);
            } catch(Exception e) {
                // log this
                LOGGER.warn(e);
            }
        }
    }
    
    private Set<File> findMatchingChecksum(Map<String, Set<File>> checksumMap, File file, String checksumString) {

        if(checksumMap.containsKey(checksumString)) {
            return checksumMap.get(checksumString);
        } else {
            return new LinkedHashSet<File>();
        }
    } 
            
    public void iterate(File root, Map<String, Set<File>> checksumMap) {

        File[] files = root.listFiles( getFileFilter() );
        iterateOverFiles(files, checksumMap);

        File[] subDirectories = root.listFiles( getDirectoryFilter() );
        for(File subDirectory : subDirectories) {
            callback.currentDirectory(subDirectory);
            iterate(subDirectory, checksumMap);
        }
    }
    
    private FileFilter getFileFilter() {
        if(ignoreHiddenDirectories) {
            return FileFilterUtils.and(FileFileFilter.FILE, HiddenFileFilter.VISIBLE);
        } else {
            return FileFileFilter.FILE;
        }
    }
    
    private FileFilter getDirectoryFilter() {
        if(ignoreHiddenFiles) {
            return FileFilterUtils.and(DirectoryFileFilter.DIRECTORY, HiddenFileFilter.VISIBLE);
        } else {
            return DirectoryFileFilter.DIRECTORY;
        }
    }

    public interface Callback {

        public void currentFile(File file);

        public void currentDirectory(File directory);
    }

    public final static Callback SYSTEM_CALLBACK = new Callback() {

        @Override
        public void currentFile(File file) {
            System.out.println(file.getAbsolutePath());
        }

        @Override
        public void currentDirectory(File directory) {
            System.out.println(directory.getAbsolutePath());
        }
    };

    public final static Callback NOOP_CALLBACK = new Callback() {

        @Override
        public void currentFile(File file) {}

        @Override
        public void currentDirectory(File directory) {}
    };

    public final static Callback LOG_CALLBACK = new Callback() {
        @Override
        public void currentFile(File file) {
            LOGGER.info(file.getAbsolutePath());
        }

        @Override
        public void currentDirectory(File directory) {
            LOGGER.info(directory.getAbsolutePath());
        }
    };
}
