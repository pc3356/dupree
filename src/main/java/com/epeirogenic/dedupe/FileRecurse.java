package com.epeirogenic.dedupe;

import org.apache.commons.io.filefilter.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.*;

public class FileRecurse {

    private Checksum checksum;
    private boolean ignoreHiddenDirectories = true;
    private boolean ignoreHiddenFiles = true;

    public FileRecurse(Checksum checksum) {
        setChecksum(checksum);
    }

    public void setChecksum(Checksum checksum) {
        this.checksum = checksum;
    }

    public void setIgnoreHiddenDirectories(boolean ignore) {
        ignoreHiddenDirectories = ignore;
    }

    public void setIgnoreHiddenFiles(boolean ignore) {
        ignoreHiddenFiles = ignore;
    }

    private void iterateOverFiles(File[] files, Map<String, Set<File>> checksumMap) {
        for(File file : files) {
            try {
                String checksumString = checksum.generateFor(file);
                Set<File> fileSet = findMatchingChecksum(checksumMap, file, checksumString);
                fileSet.add(file);
                checksumMap.put(checksumString, fileSet);
            } catch(Exception e) {
                // log this
                System.err.println(e);
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

    public interface FileRecurseCallback {

    }
}
