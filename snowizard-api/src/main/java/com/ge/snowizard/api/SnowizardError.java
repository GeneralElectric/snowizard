package com.ge.snowizard.api;

import javax.annotation.concurrent.Immutable;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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
    @JsonCreator
    public SnowizardError(@JsonProperty("code") final int code,
            @JsonProperty("message") final String message) {
        this.code = code;
        this.message = message;
    }

    @JsonProperty
    public int getCode() {
        return code;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        final SnowizardError other = (SnowizardError) obj;
        return Objects.equal(code, other.code)
                && Objects.equal(message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code, message);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("code", code)
                .add("message", message).toString();
    }
}
