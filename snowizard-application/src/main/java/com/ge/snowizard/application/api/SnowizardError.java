package com.ge.snowizard.application.api;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.Response;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ge.snowizard.application.core.MediaTypeAdditional;

@Immutable
public final class SnowizardError {

    private final int code;

    @NotEmpty
    private final String message;

    /**
     * Constructor
     *
     * @param code
     *            machine-readable error code
     * @param message
     *            human-readable error message
     */
    public SnowizardError(@JsonProperty("code") final int code,
            @JsonProperty("message") final String message) {
        this.code = code;
        this.message = message;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @JsonProperty
    public int getCode() {
        return code;
    }

    /**
     * Create a new {@link Response} object representing our standardized
     * JSON-formatted error messages.
     *
     * @param status
     *            {@link Response.Status}
     * @param message
     *            error message
     * @return {@link Response}
     */
    public static Response newResponse(final Response.Status status,
            final String message) {
        return Response.status(status)
                .entity(new SnowizardError(status.getStatusCode(), message))
                .type(MediaTypeAdditional.APPLICATION_JSON_UTF8).build();
    }
}
