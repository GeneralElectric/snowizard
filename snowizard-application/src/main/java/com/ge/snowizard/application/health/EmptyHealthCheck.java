package com.ge.snowizard.application.health;

import com.codahale.metrics.health.HealthCheck;

public class EmptyHealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
