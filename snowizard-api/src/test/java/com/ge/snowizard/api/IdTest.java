package com.ge.snowizard.api;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IdTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private final Id id = new Id(1234L);

    @Test
    public void serializesToJSON() throws Exception {
        final String actual = MAPPER.writeValueAsString(id);
        final String expected = MAPPER.writeValueAsString(MAPPER.readValue(
                fixture("fixtures/id.json"), Id.class));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        final Id actual = MAPPER.readValue(fixture("fixtures/id.json"),
                Id.class);
        assertThat(actual).isEqualsToByComparingFields(id);
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
