package com.ge.snowizard.exceptions;

public class InvalidUserAgentError extends Exception {

    /**
     * Version ID for serialization
     */
    private static final long serialVersionUID = 1L;

    public InvalidUserAgentError() {
        super();
    }

    public InvalidUserAgentError(final Throwable cause) {
        super(cause);
    }

    public InvalidUserAgentError(final String message) {
        super(message);
    }

    public InvalidUserAgentError(final String message, final Throwable cause) {
        super(message, cause);
    }
}
