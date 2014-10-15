package com.ge.snowizard.application.resources;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import io.dropwizard.jersey.protobuf.ProtocolBufferMediaType;
import io.dropwizard.jersey.protobuf.ProtocolBufferMessageBodyProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Rule;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.snowizard.api.Id;
import com.ge.snowizard.api.SnowizardError;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardResponse;
import com.ge.snowizard.application.core.MediaTypeAdditional;
import com.ge.snowizard.application.exceptions.SnowizardExceptionMapper;
import com.ge.snowizard.application.resources.IdResource;
import com.ge.snowizard.core.IdWorker;
import com.ge.snowizard.exceptions.InvalidSystemClock;
import com.ge.snowizard.exceptions.InvalidUserAgentError;
import com.sun.jersey.api.client.ClientResponse;

public class IdResourceTest {
    private final String AGENT = "test-agent";
    private final SnowizardError AGENT_ERROR = new SnowizardError(400,
            "Invalid User-Agent header");
    private final IdWorker worker = mock(IdWorker.class);

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(new SnowizardExceptionMapper())
    .addProvider(new ProtocolBufferMessageBodyProvider())
    .addResource(new IdResource(worker)).build();

    @Test
    public void testGetIdAsString() throws Exception {
        final long expected = 100L;
        when(worker.getId(AGENT)).thenReturn(expected);

        final ClientResponse response = resources.client().resource("/")
                .accept(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.USER_AGENT, AGENT)
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

        final ClientResponse response = resources.client().resource("/")
                .accept(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        final ObjectMapper mapper = resources.getObjectMapper();
        final String expected = mapper.writeValueAsString(AGENT_ERROR);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsStringInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        final ClientResponse response = resources.client().resource("/")
                .accept(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(500);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSON() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final Id actual = resources.client().resource("/")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, AGENT).get(Id.class);

        final Id expected = new Id(id);

        assertThat(actual).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        final ClientResponse response = resources.client().resource("/")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        final ObjectMapper mapper = resources.getObjectMapper();
        final String expected = mapper.writeValueAsString(AGENT_ERROR);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        final ClientResponse response = resources.client().resource("/")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(500);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONP() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final String actual = resources.client().resource("/?callback=testing")
                .accept(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
                .header(HttpHeaders.USER_AGENT, AGENT).get(String.class);

        final Id expectedId = new Id(id);

        final ObjectMapper mapper = resources.getObjectMapper();
        final StringBuilder expected = new StringBuilder("testing(");
        expected.append(mapper.writeValueAsString(expectedId));
        expected.append(")");

        assertThat(actual).isEqualTo(expected.toString());
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONPInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        final ClientResponse response = resources.client()
                .resource("/?callback=testing")
                .accept(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        final ObjectMapper mapper = resources.getObjectMapper();
        final String expected = mapper.writeValueAsString(AGENT_ERROR);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONPInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        final ClientResponse response = resources.client()
                .resource("/?callback=testing")
                .accept(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(500);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobuf() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final ClientResponse response = resources.client().resource("/")
                .accept(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        final SnowizardResponse actual = response
                .getEntity(SnowizardResponse.class);

        final SnowizardResponse expected = SnowizardResponse.newBuilder()
                .addId(id).build();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobufInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        final ClientResponse response = resources.client().resource("/")
                .accept(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        final ObjectMapper mapper = resources.getObjectMapper();
        final String expected = mapper.writeValueAsString(AGENT_ERROR);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobufInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        final ClientResponse response = resources.client().resource("/")
                .accept(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
                .header(HttpHeaders.USER_AGENT, AGENT)
                .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(500);
        verify(worker).getId(AGENT);
    }
}
