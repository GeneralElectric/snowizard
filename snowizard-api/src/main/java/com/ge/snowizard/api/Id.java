package com.ge.snowizard.api;

import javax.annotation.concurrent.Immutable;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.yammer.dropwizard.json.JsonSnakeCase;

@Immutable
@JsonSnakeCase
public final class Id {

    private final long id;

    @NotEmpty
    private final String idStr;

    /**
     * Constructor
     *
     * @param id
     * @param idStr
     */
    public Id(@JsonProperty("id") final long id,
            @JsonProperty("id_str") final String idStr) {
        this.id = id;
        this.idStr = idStr;
    }

    /**
     * Constructor
     *
     * @param id
     */
    public Id(final long id) {
        this.id = id;
        this.idStr = String.valueOf(id);
    }

    /**
     * Return the ID as a Long value
     *
     * @return the ID as a Long value
     */
    @JsonProperty
    public long getId() {
        return id;
    }

    /**
     * Return the ID as a String value
     *
     * @return the ID as a String value
     */
    @JsonProperty("id_str")
    public String getIdAsString() {
        return idStr;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        final Id other = (Id) obj;
        return Objects.equal(id, other.id) && Objects.equal(idStr, other.idStr);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, idStr);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("idStr", idStr)
                .toString();
    }
}
