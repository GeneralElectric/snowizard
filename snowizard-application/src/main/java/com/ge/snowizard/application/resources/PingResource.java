package com.ge.snowizard.application.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import io.dropwizard.jersey.caching.CacheControl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/ping")
@Api("ping")
public class PingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    @ApiOperation(value = "Ping", notes = "Pings the API service")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "pong") })
    public Response ping() {
        return Response.ok("pong").type(MediaType.TEXT_PLAIN).build();
    }
}
