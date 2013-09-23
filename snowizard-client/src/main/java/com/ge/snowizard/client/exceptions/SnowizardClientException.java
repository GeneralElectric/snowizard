package com.ge.snowizard.client.exceptions;

public class SnowizardClientException extends Exception {

    /**
     * Version ID for serialization
     */
    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    public SnowizardClientException() {
        super();
    }

    /** {@inheritDoc} */
    public SnowizardClientException(final Throwable cause) {
        super(cause);
    }

    /** {@inheritDoc} */
    public SnowizardClientException(final String message) {
        super(message);
    }

    /** {@inheritDoc} */
    public SnowizardClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
