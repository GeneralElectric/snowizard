package com.ge.snowizard.application.exceptions;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Immutable
public final class SnowizardException extends WebApplicationException {

    private static final long serialVersionUID = 1L;
    private final Response.Status status;
    private final String message;

    /**
     * Constructor
     *
     * @param code
     *            Status code to return
     * @param message
     *            Error message to return
     */
    public SnowizardException(final int code, final String message) {
        super(code);
        this.status = Response.Status.fromStatusCode(code);
        this.message = message;
    }

    /**
     * Constructor
     *
     * @param status
     *            Status code to return
     * @param message
     *            Error message to return
     */
    public SnowizardException(final Response.Status status, final String message) {
        super(status);
        this.status = status;
        this.message = message;
    }

    /**
     * Constructor
     *
     * @param code
     *            Status code to return
     * @param message
     *            Error message to return
     * @param cause
     *            Throwable which caused the exception
     */
    public SnowizardException(final Response.Status status,
            final String message, final Throwable cause) {
        super(cause, status);
        this.status = status;
        this.message = message;
    }

    public int getCode() {
        return status.getStatusCode();
    }

    public Response.Status getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
