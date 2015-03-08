package com.ge.snowizard.api;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SnowizardErrorTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private final SnowizardError error = new SnowizardError(123, "this is bad");

    @Test
    public void serializesToJSON() throws Exception {
        final String actual = MAPPER.writeValueAsString(error);
        final String expected = MAPPER.writeValueAsString(MAPPER.readValue(
                fixture("fixtures/error.json"), SnowizardError.class));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        final SnowizardError actual = MAPPER.readValue(
                fixture("fixtures/error.json"), SnowizardError.class);
        assertThat(actual).isEqualTo(error);
    }

    @Test
    public void testEquals() {
        final SnowizardError error2 = new SnowizardError(123, "this is bad");
        assertThat(error2).isEqualTo(error);
    }

    @Test
    public void testToString() {
        final String expected = "SnowizardError{code=123, message=this is bad}";
        assertThat(error.toString()).isEqualTo(expected);
    }

    @Test
    public void testHashCode() {
        assertThat(error.hashCode()).isEqualTo(-2046259593);
    }
}
