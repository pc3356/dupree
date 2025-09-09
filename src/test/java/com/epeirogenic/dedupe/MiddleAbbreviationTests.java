package com.epeirogenic.dedupe;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MiddleAbbreviationTests {

    @Test
    public void abbreviateMiddle_should_do_something_sensible() {

        String pathString = "/mnt/hgfs/workspace/olympic-apps/tripod2012/pom.xml";
        String expected = "/mnt/hgfs.../pom.xml";
        int expectedLength = 20;

        String actual = StringUtils.abbreviateMiddle(pathString, "...", 20);

        assertThat(actual).hasSize(expectedLength);
        assertThat(actual.length()).isEqualTo(expectedLength);
        assertThat(actual).isEqualTo(expected);
    }
}
