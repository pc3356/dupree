package com.epeirogenic.dedupe;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.*;

public class DedupeIntegrationTest {

    FileRecurse fileRecurse;

    @Before
    public void setUp() {
        fileRecurse = new FileRecurse(Checksum.SHA256);
    }
    
    @Test
    public void running_dedupe_over_file_tree_should_produce_map_of_checksums() throws Exception {
        
        File root = new File("/Users/administrator/Documents/OU");
        
        Map<String, Set<File>> checksums = new HashMap<String, Set<File>>();
        
        long start = System.currentTimeMillis();
        
        fileRecurse.iterate(root, checksums);
        
        long duration = System.currentTimeMillis() - start;
        
        System.out.println("Finished in " + duration + "ms");

//        for(Map.Entry<String, Set<File>> entry : checksums.entrySet()) {
//            System.out.println(entry.getKey() + " : " + entry.getValue().size());
//            for(File file : entry.getValue()) {
//                System.out.println('\t' + file.getCanonicalPath());
//            }
//        }
        
        assertTrue(checksums.size() > 0);

        Map<String, Set<File>> duplicates = new LinkedHashMap<String, Set<File>>();
        for(Map.Entry<String, Set<File>> entry : checksums.entrySet()) {
            if(entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        long bytesUsedByDupes = 0L;
        for(Map.Entry<String, Set<File>> entry : duplicates.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue().size());
            int i = 0;
            for(File dupe : entry.getValue()) {
                if(i > 0) {
                    bytesUsedByDupes += dupe.length();
                }
                i++;
                System.out.println(dupe.getCanonicalPath() + " : " + dupe.length() + 'b');
            }
        }
        System.out.println((bytesUsedByDupes / 1024L) / 1024L + " Mb used by dupes");
        
        assertEquals(3, duplicates.size());

    }

}
