package com.epeirogenic.dedupe;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.*;

public class DedupeIntegrationTest {

    FileRecurse fileRecurse;

    @Before
    public void setUp() {
        fileRecurse = new FileRecurse(Checksum.SHA256, FileRecurse.NOOP_CALLBACK);
    }
    
    @Test
    public void running_dedupe_over_file_tree_should_produce_map_of_checksums() throws Exception {

        File root = new ClassPathResource("fixtures/directory_tree").getFile();
        Map<String, Set<File>> checksums = new HashMap<String, Set<File>>();
        fileRecurse.iterate(root, checksums);
        assertThat(checksums.size(), not(0));

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
        
        assertThat(duplicates.size(), is(2));

        String checksum1 = Checksum.SHA256.generateFor(new ClassPathResource("fixtures/file1").getFile());
        assertThat(duplicates.get(checksum1).size(), is(5));

        String checksum2 = Checksum.SHA256.generateFor(new ClassPathResource("fixtures/mankini_gnome.jpg").getFile());
        assertThat(duplicates.get(checksum2).size(), is(7));
    }

    @Test
    public void running_dedupe_over_empty_directory_should_not_cause_error() throws Exception {

        Resource base = new ClassPathResource("fixtures/empty_directory");
        File root = new ClassPathResource("fixtures/empty_directory").getFile();
        Map<String, Set<File>> checksums = new HashMap<String, Set<File>>();
        fileRecurse.iterate(root, checksums);
        assertThat(checksums.size(), is(0));
    }

}
