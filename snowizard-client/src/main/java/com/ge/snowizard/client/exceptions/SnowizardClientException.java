package com.ge.snowizard.client.exceptions;

public class SnowizardClientException extends Exception {

    /**
     * Version ID for serialization
     */
    private static final long serialVersionUID = 1L;

    public SnowizardClientException() {
        super();
    }

    public SnowizardClientException(final Throwable cause) {
        super(cause);
    }

    public SnowizardClientException(final String message) {
        super(message);
    }

    public SnowizardClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
