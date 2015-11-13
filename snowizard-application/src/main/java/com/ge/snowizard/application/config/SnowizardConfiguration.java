package com.ge.snowizard.application.config;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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

    @JsonProperty
    private boolean enable_cors = false;

    @Valid
    @NotNull
    @JsonProperty
    public final SwaggerBundleConfiguration swagger = new SwaggerBundleConfiguration();

    @JsonProperty("worker_id")
    public int getWorkerId() {
        return worker_id;
    }

    @JsonProperty("datacenter_id")
    public int getDatacenterId() {
        return datacenter_id;
    }

    @JsonProperty("validate_user_agent")
    public boolean validateUserAgent() {
        return validate_user_agent;
    }

    @JsonProperty("enable_cors")
    public boolean isCORSEnabled() {
        return enable_cors;
    }

    @JsonProperty
    public SwaggerBundleConfiguration getSwagger() {
        return swagger;
    }
}
