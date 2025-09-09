package com.epeirogenic.dedupe;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
@Slf4j
public class FileRecurse {

    private final Checksum checksum;
    private final Callback callback;
    private boolean ignoreHiddenDirectories;
    private boolean ignoreHiddenFiles;

    public FileRecurse(final Checksum checksum, final Callback callback) {
        this.checksum = checksum;
        this.callback = callback;
        this.ignoreHiddenDirectories = true;
        this.ignoreHiddenFiles = true;
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
//                    log.warn("Exception in iteration", e);
                }
            }
        }
    }
    
    private Set<File> findMatchingChecksum(final Map<String, Set<File>> checksumMap, final String checksumString) {

        if(checksumMap.containsKey(checksumString)) {
            return checksumMap.get(checksumString);
        } else {
            return new LinkedHashSet<>();
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

    public final static Callback NOOP_CALLBACK = new Callback() {

        @Override
        public void currentFile(final File file) {}

        @Override
        public void currentDirectory(final File directory) {}
    };

}
