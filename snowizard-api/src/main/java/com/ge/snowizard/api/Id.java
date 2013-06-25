package com.ge.snowizard.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Id {

    @JsonProperty
    private final Long id;

    @JsonProperty
    private final String id_str;

    /**
     * Constructor
     *
     * @param id
     */
    public Id(@JsonProperty("id") final long id) {
        this.id = id;
        this.id_str = String.valueOf(id);
    }

    public long getId() {
        return id;
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
        return (id == other.id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + id.hashCode();
    }
}
