package com.ge.snowizard.service.resources;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.snowizard.api.Id;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardResponse;
import com.ge.snowizard.core.IdWorker;
import com.ge.snowizard.exceptions.InvalidSystemClock;
import com.ge.snowizard.exceptions.InvalidUserAgentError;
import com.ge.snowizard.service.api.SnowizardError;
import com.ge.snowizard.service.core.JacksonProtobufProvider;
import com.ge.snowizard.service.core.MediaTypeAdditional;
import com.ge.snowizard.service.resources.IdResource;
import com.sun.jersey.api.client.ClientResponse;
import com.yammer.dropwizard.testing.ResourceTest;

public class IdResourceTest extends ResourceTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    private final String AGENT = "Test Agent";
    private final SnowizardError AGENT_ERROR = new SnowizardError(400,
            "Invalid User-Agent header");
    private final IdWorker worker = mock(IdWorker.class);

    @Override
    protected void setUpResources() throws Exception {
        addProvider(new JacksonProtobufProvider());
        addResource(new IdResource(worker));
    }

    @Test
    public void testGetIdAsString() throws Exception {
        final long expected = 100L;
        when(worker.getId(AGENT)).thenReturn(expected);

        final ClientResponse response = client().resource("/")
                .accept(MediaType.TEXT_PLAIN).header("User-Agent", AGENT)
                .get(ClientResponse.class);
        final String entity = response.getEntity(String.class);
        final long actual = Long.valueOf(entity);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsStringInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        final ClientResponse response = client().resource("/")
                .accept(MediaType.TEXT_PLAIN).header("User-Agent", AGENT)
                .get(ClientResponse.class);

        final ObjectMapper mapper = getObjectMapperFactory().build();
        final String expected = mapper.writeValueAsString(AGENT_ERROR);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsStringInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        final ClientResponse response = client().resource("/")
                .accept(MediaType.TEXT_PLAIN).header("User-Agent", AGENT)
                .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(500);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSON() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final Id actual = client().resource("/")
                .accept(MediaType.APPLICATION_JSON).header("User-Agent", AGENT)
                .get(Id.class);

        final Id expected = new Id(id);

        assertThat(actual).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        final ClientResponse response = client().resource("/")
                .accept(MediaType.APPLICATION_JSON).header("User-Agent", AGENT)
                .get(ClientResponse.class);

        final ObjectMapper mapper = getObjectMapperFactory().build();
        final String expected = mapper.writeValueAsString(AGENT_ERROR);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        final ClientResponse response = client().resource("/")
                .accept(MediaType.APPLICATION_JSON).header("User-Agent", AGENT)
                .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(500);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONP() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final String actual = client().resource("/?callback=testing")
                .accept(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
                .header("User-Agent", AGENT).get(String.class);

        final Id expectedId = new Id(id);

        final ObjectMapper mapper = getObjectMapperFactory().build();
        final StringBuilder expected = new StringBuilder("testing(");
        expected.append(mapper.writeValueAsString(expectedId));
        expected.append(")");

        assertThat(actual).isEqualTo(expected.toString());
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONPInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        final ClientResponse response = client().resource("/?callback=testing")
                .accept(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
                .header("User-Agent", AGENT).get(ClientResponse.class);

        final ObjectMapper mapper = getObjectMapperFactory().build();
        final String expected = mapper.writeValueAsString(AGENT_ERROR);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONPInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        final ClientResponse response = client().resource("/?callback=testing")
                .accept(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
                .header("User-Agent", AGENT).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(500);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobuf() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final ClientResponse response = client().resource("/")
                .accept(MediaTypeAdditional.APPLICATION_PROTOBUF)
                .header("User-Agent", AGENT).get(ClientResponse.class);

        final SnowizardResponse actual = response
                .getEntity(SnowizardResponse.class);

        final SnowizardResponse expected = SnowizardResponse.newBuilder()
                .setId(id).build();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobufInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        final ClientResponse response = client().resource("/")
                .accept(MediaTypeAdditional.APPLICATION_PROTOBUF)
                .header("User-Agent", AGENT).get(ClientResponse.class);

        final ObjectMapper mapper = getObjectMapperFactory().build();
        final String expected = mapper.writeValueAsString(AGENT_ERROR);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobufInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        final ClientResponse response = client().resource("/")
                .accept(MediaTypeAdditional.APPLICATION_PROTOBUF)
                .header("User-Agent", AGENT).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(500);
        verify(worker).getId(AGENT);
    }
}
