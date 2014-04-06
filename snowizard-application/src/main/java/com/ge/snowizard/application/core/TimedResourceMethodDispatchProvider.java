package com.ge.snowizard.application.core;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

public class TimedResourceMethodDispatchProvider implements
        ResourceMethodDispatchProvider {

    private final ResourceMethodDispatchProvider provider;

    /**
     * Constructor
     * 
     * @param provider
     *            {@link ResourceMethodDispatchProvider}
     */
    public TimedResourceMethodDispatchProvider(
            final ResourceMethodDispatchProvider provider) {
        this.provider = provider;
    }

    @Override
    public RequestDispatcher create(
            final AbstractResourceMethod abstractResourceMethod) {
        final RequestDispatcher dispatcher = provider
                .create(abstractResourceMethod);
        final Timed timed = abstractResourceMethod.getMethod().getAnnotation(
                Timed.class);

        if (timed != null) {
            final String resourceName = abstractResourceMethod
                    .getDeclaringResource().getResourceClass().getSimpleName();
            final String methodName = abstractResourceMethod.getMethod()
                    .getName();
            return new TimedRequestDispatcher(dispatcher, String.format(
                    "%s/%s", resourceName, methodName));
        }
        return dispatcher;
    }
}
