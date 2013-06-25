package com.ge.snowizard.service;

import static org.mockito.Mockito.*;
import org.junit.Test;
import com.ge.snowizard.service.SnowizardConfiguration;
import com.ge.snowizard.service.SnowizardService;
import com.ge.snowizard.service.resources.IdResource;
import com.yammer.dropwizard.config.Environment;

public class SnowizardServiceTest {
    private final Environment environment = mock(Environment.class);
    private final SnowizardService service = new SnowizardService();
    private final SnowizardConfiguration config = new SnowizardConfiguration();

    @Test
    public void buildsAIdResource() throws Exception {
        service.run(config, environment);

        verify(environment).addResource(any(IdResource.class));
    }
}
