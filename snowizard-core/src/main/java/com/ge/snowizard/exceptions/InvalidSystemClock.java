package com.ge.snowizard.exceptions;

public class InvalidSystemClock extends Exception {

    /**
     * Version ID for serialization
     */
    private static final long serialVersionUID = 1L;

    public InvalidSystemClock() {
        super();
    }

    public InvalidSystemClock(final Throwable cause) {
        super(cause);
    }

    public InvalidSystemClock(final String message) {
        super(message);
    }

    public InvalidSystemClock(final String message, final Throwable cause) {
        super(message, cause);
    }
}
