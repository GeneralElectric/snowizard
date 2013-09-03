package com.ge.snowizard.api.protos;

import static org.fest.assertions.api.Assertions.assertThat;
import org.junit.Test;

import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardResponse;

public class SnowizardProtosTest {

    private final SnowizardResponse id = SnowizardResponse.newBuilder()
            .setId(1234L).build();

    @Test
    public void testSnowizardResponse() {
        assertThat(id.getId()).isEqualTo(1234L);
        assertThat(id.hasId()).isTrue();

        final SnowizardResponse id2 = SnowizardResponse.newBuilder()
                .setId(1234L).build();
        assertThat(id2).isEqualTo(id2);
    }
}
