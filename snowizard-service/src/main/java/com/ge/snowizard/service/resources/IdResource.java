package com.ge.snowizard.service.resources;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
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
import com.yammer.metrics.annotation.Timed;

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

    @GET
    @Timed
    @Produces(MediaType.TEXT_PLAIN)
    public String getIdAsString(@HeaderParam("User-Agent") final String agent) {
        return String.valueOf(getId(agent));
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Id getIdAsJSON(@HeaderParam("User-Agent") final String agent) {
        return new Id(getId(agent));
    }

    @GET
    @Timed
    @Produces(MediaTypeAdditional.APPLICATION_JAVASCRIPT)
    public JSONWithPadding getIdAsJSONP(
            @HeaderParam("User-Agent") final String agent,
            @QueryParam("callback") @DefaultValue("callback") final String callback) {
        return new JSONWithPadding(getIdAsJSON(agent), callback);
    }

    @GET
    @Timed
    @Produces(MediaTypeAdditional.APPLICATION_PROTOBUF)
    public SnowizardResponse getIdAsProtobuf(
            @HeaderParam("User-Agent") final String agent) {
        return SnowizardResponse.newBuilder().setId(getId(agent)).build();
    }
}
