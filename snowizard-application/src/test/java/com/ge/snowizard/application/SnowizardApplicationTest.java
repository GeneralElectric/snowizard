package com.ge.snowizard.application;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.File;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import com.codahale.metrics.MetricRegistry;
import com.ge.snowizard.application.SnowizardApplication;
import com.ge.snowizard.application.config.SnowizardConfiguration;
import com.ge.snowizard.application.resources.IdResource;
import com.ge.snowizard.application.resources.PingResource;
import com.ge.snowizard.application.resources.VersionResource;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;

public class SnowizardApplicationTest {
    private final String AGENT = "snowizard-client";
    private final Environment environment = mock(Environment.class);
    private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
    private final MetricRegistry metrics = mock(MetricRegistry.class);
    private final SnowizardApplication application = new SnowizardApplication();
    private final SnowizardConfiguration config = new SnowizardConfiguration();

    @ClassRule
    public static final DropwizardAppRule<SnowizardConfiguration> RULE = new DropwizardAppRule<SnowizardConfiguration>(
            SnowizardApplication.class, resourceFilePath("test-snowizard.yml"));

    @Before
    public void setUp() {
        when(environment.jersey()).thenReturn(jersey);
        when(environment.metrics()).thenReturn(metrics);
    }

    @Test
    public void buildsAIdResource() throws Exception {
        application.run(config, environment);
        verify(jersey).register(isA(IdResource.class));
    }

    @Test
    public void buildsAPingResource() throws Exception {
        application.run(config, environment);
        verify(jersey).register(isA(PingResource.class));
    }

    @Test
    public void buildsAVersionResource() throws Exception {
        application.run(config, environment);
        verify(jersey).register(isA(VersionResource.class));
    }

    @Test
    public void testCanGetIdOverHttp() throws Exception {
        final String response = new Client()
        .resource("http://localhost:" + RULE.getLocalPort())
        .accept(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.USER_AGENT, AGENT).get(String.class);
        final long id = Long.valueOf(response);
        assertThat(id).isNotNull();
    }

    public static String resourceFilePath(final String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation)
                    .toURI()).getAbsolutePath();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
