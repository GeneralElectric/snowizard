package com.ge.snowizard.service.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.yammer.dropwizard.jersey.caching.CacheControl;

@Path("/version")
public class SnowizardResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @CacheControl(mustRevalidate = true, noCache = true, noStore = true)
    public Response getVersion() {
        return Response.ok(getClass().getPackage().getImplementationVersion())
                .type(MediaType.TEXT_PLAIN).build();
    }
}
