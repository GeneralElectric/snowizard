package com.ge.snowizard.service;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SnowizardConfiguration extends Configuration {
    @Min(1)
    @Max(1024)
    @JsonProperty
    private Integer worker_id = 1;

    @Min(1)
    @Max(1024)
    @JsonProperty
    private Integer datacenter_id = 1;

    public final Integer getWorkerId() {
        return worker_id;
    }

    public final Integer getDatacenterId() {
        return datacenter_id;
    }
}
