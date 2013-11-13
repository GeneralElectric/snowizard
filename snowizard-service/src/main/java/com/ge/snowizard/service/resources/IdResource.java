package com.ge.snowizard.service.resources;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardBatchResponse;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ge.snowizard.api.Id;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardResponse;
import com.ge.snowizard.core.IdWorker;
import com.ge.snowizard.exceptions.InvalidSystemClock;
import com.ge.snowizard.exceptions.InvalidUserAgentError;
import com.ge.snowizard.service.api.SnowizardError;
import com.ge.snowizard.service.core.MediaTypeAdditional;
import com.sun.jersey.api.json.JSONWithPadding;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import com.yammer.metrics.annotation.Timed;

import java.util.ArrayList;

@Path("/")
public class IdResource {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IdResource.class);
    private final IdWorker worker;

    /**
     * Constructor
     *
     * @param worker
     */
    public IdResource(final IdWorker worker) {
        this.worker = checkNotNull(worker);
    }

    /**
     * Get a new ID and handle any thrown exceptions
     *
     * @param agent
     *            User Agent
     * @return generated ID
     * @throws WebApplicationException
     */
    public long getId(final String agent) {
        try {
            return worker.getId(agent);
        } catch (InvalidUserAgentError e) {
            LOGGER.error("Invalid user agent ({})", agent);
            throw new WebApplicationException(SnowizardError.newResponse(
                    Status.BAD_REQUEST, "Invalid User-Agent header"));
        } catch (InvalidSystemClock e) {
            LOGGER.error("Invalid system clock", e);
            throw new WebApplicationException(SnowizardError.newResponse(
                    Status.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    public ArrayList<Long> getIds(int n, final String agent) {

        ArrayList<Long> ids = new ArrayList<Long>();
        for(int i=0; i < n; i++) {
            ids.add(getId(agent));
        }
        return ids;
    }

    /**
     * Get a new ID as plain text
     *
     * @param agent
     *            User Agent
     * @responseMessage 400 Invalid User-Agent header
     * @responseMessage 500 Invalid system clock
     * @return generated ID
     */
    @GET
    @Timed
    @Produces(MediaType.TEXT_PLAIN)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    public String getIdAsString(@HeaderParam("User-Agent") final String agent) {
        return String.valueOf(getId(agent));
    }

    /**
     * Get a new ID as JSON
     *
     * @param agent
     *            User Agent
     * @responseMessage 400 Invalid User-Agent header
     * @responseMessage 500 Invalid system clock
     * @return generated ID
     */
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    public Id getIdAsJSON(@HeaderParam("User-Agent") final String agent) {
        return new Id(getId(agent));
    }

    /**
     * Get a new ID as JSONP
     *
     * @param agent
     *            User Agent
     * @responseMessage 400 Invalid User-Agent header
     * @responseMessage 500 Invalid system clock
     * @return generated ID
     */
    @GET
    @Timed
    @Produces(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    public JSONWithPadding getIdAsJSONP(
            @HeaderParam("User-Agent") final String agent,
            @QueryParam("callback") @DefaultValue("callback") final String callback) {
        return new JSONWithPadding(getIdAsJSON(agent), callback);
    }

    /**
     * Get a new ID as a Google Protocol Buffer response
     *
     * @param agent
     *            User Agent
     * @responseMessage 400 Invalid User-Agent header
     * @responseMessage 500 Invalid system clock
     * @return generated ID
     */
    @GET
    @Timed
    @Produces(MediaTypeAdditional.APPLICATION_PROTOBUF)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    public SnowizardResponse getIdAsProtobuf(
            @HeaderParam("User-Agent") final String agent) {
        return SnowizardResponse.newBuilder().setId(getId(agent)).build();
    }

    /**
     * Get a new batch of IDs as a Google Protocol Buffer response
     *
     * @param agent
     *            User Agent
     * @responseMessage 400 Invalid User-Agent header
     * @responseMessage 500 Invalid system clock
     * @return batch of generated ID
     */
    @POST
    @Timed
    @Produces(MediaTypeAdditional.APPLICATION_PROTOBUF)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    public SnowizardBatchResponse getBatchIdAsProtobuf(byte[] body,
            @HeaderParam("User-Agent") final String agent) {
        SnowizardRequest req;
        try {
            req = SnowizardRequest.parseFrom(body);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Failed decoding protobuf request");
            throw new WebApplicationException(SnowizardError.newResponse(
                    Status.INTERNAL_SERVER_ERROR, e.getMessage()));

        }
        return SnowizardBatchResponse.newBuilder().addAllIds(getIds(req.getBatchSize(), agent)).build();
    }
}
