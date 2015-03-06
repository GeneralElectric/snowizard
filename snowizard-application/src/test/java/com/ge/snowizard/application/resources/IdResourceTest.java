package com.ge.snowizard.application.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;
import io.dropwizard.jersey.protobuf.ProtocolBufferMediaType;
import io.dropwizard.jersey.protobuf.ProtocolBufferMessageBodyProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientResponse;
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

        final Response response = resources.client().target("/")
                .request(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.USER_AGENT, AGENT).get();
        final String entity = response.readEntity(String.class);
        final long actual = Long.valueOf(entity);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsStringInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        try {
            resources.client().target("/").request(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.USER_AGENT, AGENT)
                    .get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(BadRequestException.class);
        } catch (final BadRequestException e) {
            final ObjectMapper mapper = resources.getObjectMapper();
            final String expected = mapper.writeValueAsString(AGENT_ERROR);

            final Response response = e.getResponse();
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.readEntity(String.class)).isEqualTo(expected);
        }

        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsStringInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        try {
            resources.client().target("/").request(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.USER_AGENT, AGENT)
            .get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(InternalServerErrorException.class);
        } catch (final InternalServerErrorException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(500);
        }

        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSON() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final Id actual = resources.client().target("/")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, AGENT).get(Id.class);

        final Id expected = new Id(id);

        assertThat(actual).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        try {
            resources.client().target("/").request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.USER_AGENT, AGENT)
            .get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(BadRequestException.class);
        } catch (final BadRequestException e) {
            final ObjectMapper mapper = resources.getObjectMapper();
            final String expected = mapper.writeValueAsString(AGENT_ERROR);

            final Response response = e.getResponse();
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.readEntity(String.class)).isEqualTo(expected);
        }

        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        try {
            resources.client().target("/").request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.USER_AGENT, AGENT)
                    .get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(InternalServerErrorException.class);
        } catch (final InternalServerErrorException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(500);
        }

        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONP() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final String actual = resources.client().target("/?callback=testing")
                .request(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
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

        try {
            resources.client().target("/?callback=testing")
            .request(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
            .header(HttpHeaders.USER_AGENT, AGENT)
            .get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(BadRequestException.class);
        } catch (final BadRequestException e) {
            final ObjectMapper mapper = resources.getObjectMapper();
            final String expected = mapper.writeValueAsString(AGENT_ERROR);

            final Response response = e.getResponse();
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.readEntity(String.class)).isEqualTo(expected);
        }

        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsJSONPInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        try {
            resources.client().target("/?callback=testing")
                    .request(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
                    .header(HttpHeaders.USER_AGENT, AGENT)
                    .get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(InternalServerErrorException.class);
        } catch (final InternalServerErrorException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(500);
        }

        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobuf() throws Exception {
        final long id = 100L;
        when(worker.getId(AGENT)).thenReturn(id);

        final Response response = resources.client().target("/")
                .register(new ProtocolBufferMessageBodyProvider())
                .request(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
                .header(HttpHeaders.USER_AGENT, AGENT).get();

        final SnowizardResponse actual = response
                .readEntity(SnowizardResponse.class);

        final SnowizardResponse expected = SnowizardResponse.newBuilder()
                .addId(id).build();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual).isEqualTo(expected);
        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobufInvalidAgent() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidUserAgentError());

        try {
            resources.client().target("/")
            .request(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
                    .header(HttpHeaders.USER_AGENT, AGENT)
                    .get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(BadRequestException.class);
        } catch (final BadRequestException e) {
            final ObjectMapper mapper = resources.getObjectMapper();
            final String expected = mapper.writeValueAsString(AGENT_ERROR);

            final Response response = e.getResponse();
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.readEntity(String.class)).isEqualTo(expected);
        }

        verify(worker).getId(AGENT);
    }

    @Test
    public void testGetIdAsProtobufInvalidClock() throws Exception {
        when(worker.getId(AGENT)).thenThrow(new InvalidSystemClock());

        try {
            resources.client().target("/")
                    .request(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
                    .header(HttpHeaders.USER_AGENT, AGENT)
                    .get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(InternalServerErrorException.class);
        } catch (final InternalServerErrorException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(500);
        }

        verify(worker).getId(AGENT);
    }
}
