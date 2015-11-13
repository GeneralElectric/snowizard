package com.ge.snowizard.application;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.ge.snowizard.application.config.SnowizardConfiguration;
import com.ge.snowizard.application.exceptions.SnowizardExceptionMapper;
import com.ge.snowizard.application.filter.CharsetResponseFilter;
import com.ge.snowizard.application.health.EmptyHealthCheck;
import com.ge.snowizard.application.resources.IdResource;
import com.ge.snowizard.application.resources.PingResource;
import com.ge.snowizard.application.resources.VersionResource;
import com.ge.snowizard.core.IdWorker;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.protobuf.ProtocolBufferMessageBodyProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

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
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(new SwaggerBundle<SnowizardConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
                    final SnowizardConfiguration configuration) {
                return configuration.getSwagger();
            }
        });
    }

    @Override
    public void run(final SnowizardConfiguration config,
            final Environment environment) throws Exception {

        environment.jersey().register(SnowizardExceptionMapper.class);
        environment.jersey().register(ProtocolBufferMessageBodyProvider.class);
        environment.jersey().register(CharsetResponseFilter.class);

        if (config.isCORSEnabled()) {
            final FilterRegistration.Dynamic filter = environment.servlets()
                    .addFilter("CrossOriginFilter", CrossOriginFilter.class);
            filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),
                    true, "/*");
            filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM,
                    "GET");
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

        environment.metrics().register(MetricRegistry
                .name(SnowizardApplication.class, "datacenter_id"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return config.getDatacenterId();
                    }
                });

        // health check
        environment.healthChecks().register("empty", new EmptyHealthCheck());

        // resources
        environment.jersey().register(new IdResource(worker));
        environment.jersey().register(new PingResource());
        environment.jersey().register(new VersionResource());
    }
}
