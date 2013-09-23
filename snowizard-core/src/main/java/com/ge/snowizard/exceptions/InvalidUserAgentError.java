package com.ge.snowizard.exceptions;

public class InvalidUserAgentError extends Exception {

    /**
     * Version ID for serialization
     */
    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    public InvalidUserAgentError() {
        super();
    }

    /** {@inheritDoc} */
    public InvalidUserAgentError(final Throwable cause) {
        super(cause);
    }

    /** {@inheritDoc} */
    public InvalidUserAgentError(final String message) {
        super(message);
    }

    /** {@inheritDoc} */
    public InvalidUserAgentError(final String message, final Throwable cause) {
        super(message, cause);
    }
}
