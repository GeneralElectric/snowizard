package com.ge.snowizard.application.resources;

import io.dropwizard.jersey.caching.CacheControl;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/version")
public class VersionResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    public Response getVersion() {
        return Response.ok(getClass().getPackage().getImplementationVersion())
                .type(MediaType.TEXT_PLAIN).build();
    }
}
