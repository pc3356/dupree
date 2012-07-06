package com.epeirogenic.dedupe;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MiddleAbbreviationTests {

    @Test
    public void abbreviateMiddle_should_do_something_sensible() {

        String pathString = "/mnt/hgfs/workspace/olympic-apps/tripod2012/pom.xml";
        String expected = "/mnt/hgfs.../pom.xml";
        int expectedLength = 20;

        String actual = StringUtils.abbreviateMiddle(pathString, "...", 20);

        assertThat(actual.length(), is(expectedLength));
        assertThat(actual, is(expected));
    }
}
