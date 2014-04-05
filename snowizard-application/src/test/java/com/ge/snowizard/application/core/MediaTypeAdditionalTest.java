package com.ge.snowizard.application.core;

import static org.fest.assertions.api.Assertions.assertThat;
import org.junit.Test;
import com.ge.snowizard.application.core.MediaTypeAdditional;

public class MediaTypeAdditionalTest {

    @Test
    public void testAdditionalMediaTypes() {
        assertThat(MediaTypeAdditional.APPLICATION_JSON_UTF8).isEqualTo(
                "application/json; charset=UTF-8");
        assertThat(MediaTypeAdditional.APPLICATION_PROTOBUF).isEqualTo(
                "application/x-protobuf");
        assertThat(MediaTypeAdditional.APPLICATION_JAVASCRIPT).isEqualTo(
                "application/javascript");
    }
}
