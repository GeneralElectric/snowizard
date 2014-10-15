package com.ge.snowizard.application;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import io.dropwizard.Application;
import io.dropwizard.jersey.protobuf.ProtocolBufferMessageBodyProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.ge.snowizard.application.config.SnowizardConfiguration;
import com.ge.snowizard.application.core.TimedResourceMethodDispatchAdapter;
import com.ge.snowizard.application.exceptions.SnowizardExceptionMapper;
import com.ge.snowizard.application.resources.IdResource;
import com.ge.snowizard.application.resources.PingResource;
import com.ge.snowizard.application.resources.VersionResource;
import com.ge.snowizard.core.IdWorker;

public class SnowizardApplication extends Application<SnowizardConfiguration> {
    public static void main(final String[] args) throws Exception {
        new SnowizardApplication().run(args);
    }

    @Override
    public String getName() {
        return "snowizard";
    }

    @Override
    public void initialize(final Bootstrap<SnowizardConfiguration> bootstrap) {
        // nothing to initialize
    }

    @Override
    public void run(final SnowizardConfiguration config,
            final Environment environment) throws Exception {

        environment.jersey().register(new SnowizardExceptionMapper());
        environment.jersey().register(new ProtocolBufferMessageBodyProvider());
        environment.jersey().register(new TimedResourceMethodDispatchAdapter());

        if (config.isCORSEnabled()) {
            final FilterRegistration.Dynamic cors = environment.servlets()
                    .addFilter("CrossOriginFilter", CrossOriginFilter.class);
            cors.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),
                    true, "/*");
            cors.setInitParameter("allowedMethods", "GET");
        }

        final IdWorker worker = new IdWorker(config.getWorkerId(),
                config.getDatacenterId(), 0L, config.validateUserAgent(),
                environment.metrics());

        environment.metrics().register(
                MetricRegistry.name(SnowizardApplication.class, "worker_id"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return config.getWorkerId();
                    }
                });

        environment.metrics()
        .register(
                MetricRegistry.name(SnowizardApplication.class,
                        "datacenter_id"), new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return config.getDatacenterId();
                    }
                });

        // resources
        environment.jersey().register(new IdResource(worker));
        environment.jersey().register(new PingResource());
        environment.jersey().register(new VersionResource());
    }
}
