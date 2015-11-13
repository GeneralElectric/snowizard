package com.ge.snowizard.application.resources;

import javax.annotation.Nonnull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.dropwizard.jersey.caching.CacheControl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/version")
@Api("version")
public class VersionResource {

    private final String version;

    /**
     * Constructor
     */
    public VersionResource() {
        version = getClass().getPackage().getImplementationVersion();
    }

    /**
     * Constructor
     *
     * @param version
     *            Version to expose in the endpoint
     */
    @VisibleForTesting
    public VersionResource(@Nonnull final String version) {
        this.version = Preconditions.checkNotNull(version);
    }

    @GET
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    @ApiOperation(value = "Get Version", notes = "Returns the service version")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Service Version") })
    public Response getVersion() {
        return Response.ok(version).type(MediaType.TEXT_PLAIN).build();
    }
}
