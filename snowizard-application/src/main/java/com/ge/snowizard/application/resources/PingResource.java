package com.ge.snowizard.application.resources;

import io.dropwizard.jersey.caching.CacheControl;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ping")
public class PingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    public Response ping() {
        return Response.ok("pong").type(MediaType.TEXT_PLAIN).build();
    }
}
