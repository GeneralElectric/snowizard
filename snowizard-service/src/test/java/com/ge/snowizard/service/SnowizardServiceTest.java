package com.ge.snowizard.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.io.File;
import javax.ws.rs.core.MediaType;
import org.junit.ClassRule;
import org.junit.Test;
import com.ge.snowizard.service.SnowizardConfiguration;
import com.ge.snowizard.service.SnowizardService;
import com.ge.snowizard.service.resources.IdResource;
import com.ge.snowizard.service.resources.PingResource;
import com.ge.snowizard.service.resources.VersionResource;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;

public class SnowizardServiceTest {
    private final String AGENT = "snowizard-client";
    private final Environment environment = mock(Environment.class);
    private final SnowizardService service = new SnowizardService();
    private final SnowizardConfiguration config = new SnowizardConfiguration();

    @ClassRule
    public static final DropwizardServiceRule<SnowizardConfiguration> RULE = new DropwizardServiceRule<SnowizardConfiguration>(
            SnowizardService.class, resourceFilePath("test-snowizard.yml"));

    @Test
    public void buildsAIdResource() throws Exception {
        service.run(config, environment);
        verify(environment, times(3)).addResource(any(IdResource.class));
        verify(environment, times(3)).addResource(any(PingResource.class));
        verify(environment, times(3)).addResource(any(VersionResource.class));
    }

    @Test
    public void testCanGetIdOverHttp() throws Exception {
        final String response = new Client()
                .resource("http://localhost:" + RULE.getLocalPort())
                .accept(MediaType.TEXT_PLAIN).header("User-Agent", AGENT)
                .get(String.class);
        final long id = Long.valueOf(response);
        assertThat(id).isNotNull();
    }

    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation)
                    .toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
