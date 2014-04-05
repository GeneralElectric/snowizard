package com.ge.snowizard.application;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Environment;
import com.ge.snowizard.application.core.CorsHeadersFilter;
import com.ge.snowizard.application.core.JacksonProtobufProvider;
import com.ge.snowizard.application.core.TimedResourceMethodDispatchAdapter;
import com.ge.snowizard.application.resources.IdResource;
import com.ge.snowizard.application.resources.PingResource;
import com.ge.snowizard.application.resources.VersionResource;
import com.ge.snowizard.core.IdWorker;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;

public class SnowizardApplication extends Application<SnowizardConfiguration> {
    public static void main(final String[] args) throws Exception {
        new SnowizardApplication().run(args);
    }

    @Override
    public String getName() {
        return "snowizard";
    }

    @Override
    public void initialize(
            final io.dropwizard.setup.Bootstrap<SnowizardConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/apidocs", "/apidocs",
                "index.html"));
    }

    @Override
    public void run(final SnowizardConfiguration config,
            final Environment environment) throws Exception {

        environment.jersey().register(new JacksonProtobufProvider());
        environment.jersey().register(new TimedResourceMethodDispatchAdapter());
        if (config.isCORSEnabled()) {
            environment.getApplicationContext().addFilter(
                    CorsHeadersFilter.class, "/*",
                    EnumSet.of(DispatcherType.REQUEST));
        }

        final IdWorker worker = new IdWorker(config.getWorkerId(),
                config.getDatacenterId(), config.validateUserAgent());

        Metrics.newGauge(SnowizardApplication.class, "worker_id",
                new Gauge<Integer>() {
                    @Override
                    public Integer value() {
                        return config.getWorkerId();
                    }
                });

        Metrics.newGauge(SnowizardApplication.class, "datacenter_id",
                new Gauge<Integer>() {
                    @Override
                    public Integer value() {
                        return config.getDatacenterId();
                    }
                });

        environment.jersey().register(new IdResource(worker));
        environment.jersey().register(new PingResource());
        environment.jersey().register(new VersionResource());
    }
}
