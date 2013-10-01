package com.ge.snowizard.service;

import com.ge.snowizard.core.IdWorker;
import com.ge.snowizard.service.core.CorsHeadersFilter;
import com.ge.snowizard.service.core.JacksonProtobufProvider;
import com.ge.snowizard.service.core.TimedResourceMethodDispatchAdapter;
import com.ge.snowizard.service.resources.IdResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;

public class SnowizardService extends Service<SnowizardConfiguration> {
    public static void main(final String[] args) throws Exception {
        new SnowizardService().run(args);
    }

    @Override
    public void initialize(final Bootstrap<SnowizardConfiguration> bootstrap) {
        bootstrap.setName("snowizard");
        bootstrap.addBundle(new AssetsBundle("/apidocs", "/apidocs",
                "index.html"));
    }

    @Override
    public void run(final SnowizardConfiguration config,
            final Environment environment) throws Exception {

        environment.addProvider(new JacksonProtobufProvider());
        environment.addProvider(new TimedResourceMethodDispatchAdapter());
        environment.addFilter(new CorsHeadersFilter(), "/*");

        final IdWorker worker = new IdWorker(config.getWorkerId(),
                config.getDatacenterId());

        Metrics.newGauge(SnowizardService.class, "worker_id",
                new Gauge<Integer>() {
                    @Override
                    public Integer value() {
                        return config.getWorkerId();
                    }
                });

        Metrics.newGauge(SnowizardService.class, "datacenter_id",
                new Gauge<Integer>() {
                    @Override
                    public Integer value() {
                        return config.getDatacenterId();
                    }
                });

        environment.addResource(new IdResource(worker));
    }
}
