package com.epeirogenic.dedupe;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class FileRecurse {

    private final static Logger LOGGER = Logger.getLogger(FileRecurse.class);

    private final Checksum checksum;
    private final Callback callback;
    private boolean ignoreHiddenDirectories = true;
    private boolean ignoreHiddenFiles = true;

    public FileRecurse(final Checksum checksum, final Callback callback) {
        this.checksum = checksum;
        this.callback = callback;
    }

    public void setIgnoreHiddenDirectories(final boolean ignore) {
        ignoreHiddenDirectories = ignore;
    }

    public void setIgnoreHiddenFiles(final boolean ignore) {
        ignoreHiddenFiles = ignore;
    }

    private void iterateOverFiles(File[] files, Map<String, Set<File>> checksumMap) {
        if(files != null) {
            for (final File file : files) {
                callback.currentFile(file);
                try {
                    final String checksumString = checksum.generateFor(file);
                    final Set<File> fileSet = findMatchingChecksum(checksumMap, checksumString);
                    fileSet.add(file);
                    checksumMap.put(checksumString, fileSet);
                } catch (Exception e) {
                    // log this
                    LOGGER.warn(e);
                }
            }
        }
    }
    
    private Set<File> findMatchingChecksum(final Map<String, Set<File>> checksumMap, final String checksumString) {

        if(checksumMap.containsKey(checksumString)) {
            return checksumMap.get(checksumString);
        } else {
            return new LinkedHashSet<File>();
        }
    } 
            
    public void iterate(final File root, final Map<String, Set<File>> checksumMap) {

        final File[] files = root.listFiles( getFileFilter() );
        iterateOverFiles(files, checksumMap);

        final File[] subDirectories = root.listFiles( getDirectoryFilter() );
        if(subDirectories != null) {
            for (final File subDirectory : subDirectories) {
                callback.currentDirectory(subDirectory);
                iterate(subDirectory, checksumMap);
            }
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

        void currentFile(File file);

        void currentDirectory(File directory);
    }

    public final static Callback SYSTEM_CALLBACK = new Callback() {

        @Override
        public void currentFile(final File file) {
            System.out.println(file.getAbsolutePath());
        }

        @Override
        public void currentDirectory(final File directory) {
            System.out.println(directory.getAbsolutePath());
        }
    };

    public final static Callback NOOP_CALLBACK = new Callback() {

        @Override
        public void currentFile(final File file) {}

        @Override
        public void currentDirectory(final File directory) {}
    };

    public final static Callback LOG_CALLBACK = new Callback() {
        @Override
        public void currentFile(final File file) {
            LOGGER.info(file.getAbsolutePath());
        }

        @Override
        public void currentDirectory(final File directory) {
            LOGGER.info(directory.getAbsolutePath());
        }
    };
}
