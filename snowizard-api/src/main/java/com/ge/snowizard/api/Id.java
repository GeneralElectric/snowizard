package com.ge.snowizard.api;

import io.dropwizard.jackson.JsonSnakeCase;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

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
     *            Generated ID
     * @param idStr
     *            Generated ID as a string
     */
    @JsonCreator
    public Id(@JsonProperty("id") final long id,
            @JsonProperty("id_str") final String idStr) {
        this.id = id;
        this.idStr = idStr;
    }

    /**
     * Constructor
     * 
     * @param id
     *            Generated ID
     */
    public Id(final long id) {
        this.id = id;
        this.idStr = String.valueOf(id);
    }

    /**
     * Return the ID as a long value
     * 
     * @return the ID as a long value
     */
    @JsonProperty
    public long getId() {
        return id;
    }

    /**
     * Return the ID as a string value
     * 
     * @return the ID as a string value
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
        return Objects.equals(id, other.id)
                && Objects.equals(idStr, other.idStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idStr);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id)
                .add("idStr", idStr).toString();
    }
}
