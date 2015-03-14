package com.ge.snowizard.api.protos;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardResponse;

public class SnowizardProtosTest {

    private final SnowizardResponse id = SnowizardResponse.newBuilder()
            .addId(1234L).build();

    @Test
    public void testSnowizardResponse() {
        assertThat(id.getIdCount()).isEqualTo(1);
        assertThat(id.getId(0)).isEqualTo(1234L);

        final SnowizardResponse id2 = SnowizardResponse.newBuilder()
                .addId(1234L).build();
        assertThat(id2).isEqualTo(id2);
    }
}
