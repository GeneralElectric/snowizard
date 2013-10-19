package com.ge.snowizard.service;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SnowizardConfiguration extends Configuration {
    private static final int MAX_ID = 1024;

    @Min(1)
    @Max(MAX_ID)
    @JsonProperty
    private int worker_id = 1;

    @Min(1)
    @Max(MAX_ID)
    @JsonProperty
    private int datacenter_id = 1;

    @JsonProperty
    private boolean validate_user_agent = false;

    public int getWorkerId() {
        return worker_id;
    }

    public int getDatacenterId() {
        return datacenter_id;
    }

    @JsonProperty("validate_user_agent")
    public boolean validateUserAgent() {
        return validate_user_agent;
    }
}
