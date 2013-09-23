package com.ge.snowizard.exceptions;

public class InvalidSystemClock extends Exception {

    /**
     * Version ID for serialization
     */
    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    public InvalidSystemClock() {
        super();
    }

    /** {@inheritDoc} */
    public InvalidSystemClock(final Throwable cause) {
        super(cause);
    }

    /** {@inheritDoc} */
    public InvalidSystemClock(final String message) {
        super(message);
    }

    /** {@inheritDoc} */
    public InvalidSystemClock(final String message, final Throwable cause) {
        super(message, cause);
    }
}
