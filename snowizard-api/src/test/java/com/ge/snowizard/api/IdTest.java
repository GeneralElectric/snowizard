package com.ge.snowizard.api;

import static io.dropwizard.testing.FixtureHelpers.*;
import static org.fest.assertions.api.Assertions.assertThat;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IdTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private final Id id = new Id(1234L);

    @Test
    public void serializesToJSON() throws Exception {
        assertThat(MAPPER.writeValueAsString(id)).isEqualTo(
                fixture("fixtures/id.json"));
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        assertThat(MAPPER.readValue(fixture("fixtures/id.json"), Id.class))
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
