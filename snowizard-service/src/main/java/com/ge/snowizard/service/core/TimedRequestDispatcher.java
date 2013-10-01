package com.ge.snowizard.service.core;

import com.newrelic.api.agent.NewRelic;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

public class TimedRequestDispatcher implements RequestDispatcher {

    private final String transactionName;
    private final RequestDispatcher dispatcher;

    /**
     * Constructor
     *
     * @param dispatcher
     *            {@link RequestDispatcher}
     * @param transactionName
     *            Name of transaction
     */
    public TimedRequestDispatcher(final RequestDispatcher dispatcher, final String transactionName) {
        this.dispatcher = dispatcher;
        this.transactionName = transactionName;
    }

    @Override
    public void dispatch(final Object resource, final HttpContext context) {
        NewRelic.setTransactionName(null, transactionName);
        dispatcher.dispatch(resource, context);
    }
}
