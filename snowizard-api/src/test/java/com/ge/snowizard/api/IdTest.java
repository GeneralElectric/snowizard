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

    @Test
    public void testEquals() {
        final Id id2 = new Id(1234L);
        assertThat(id2).isEqualTo(id);
    }

    @Test
    public void testToString() {
        final String expected = "Id{id=1234, idStr=1234}";
        assertThat(id.toString()).isEqualTo(expected);
    }

    @Test
    public void testHashCode() {
        assertThat(id.hashCode()).isEqualTo(1548657);
    }
}
