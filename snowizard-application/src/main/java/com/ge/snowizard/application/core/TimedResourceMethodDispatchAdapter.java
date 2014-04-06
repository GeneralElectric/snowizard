package com.ge.snowizard.application.core;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;

public class TimedResourceMethodDispatchAdapter implements
        ResourceMethodDispatchAdapter {

    @Override
    public ResourceMethodDispatchProvider adapt(
            final ResourceMethodDispatchProvider provider) {
        return new TimedResourceMethodDispatchProvider(provider);
    }
}
