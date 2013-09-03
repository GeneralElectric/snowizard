package com.ge.snowizard.api;

import static com.yammer.dropwizard.testing.JsonHelpers.*;
import static org.fest.assertions.api.Assertions.assertThat;
import org.junit.Test;

public class IdTest {

    private final Id id = new Id(1234L);

    @Test
    public void serializesToJSON() throws Exception {
        assertThat(asJson(id)).isEqualTo(jsonFixture("fixtures/id.json"));
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        assertThat(fromJson(jsonFixture("fixtures/id.json"), Id.class))
                .isEqualTo(id);
    }
}
