package com.ge.snowizard.client.integration;

import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import com.ge.snowizard.application.SnowizardApplication;
import com.ge.snowizard.application.config.SnowizardConfiguration;
import com.ge.snowizard.client.SnowizardClient;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

public class SnowizardClientIT {

    private static final int COUNT = 1000;
    private SnowizardClient client;

    @ClassRule
    public static final DropwizardAppRule<SnowizardConfiguration> RULE = new DropwizardAppRule<SnowizardConfiguration>(
            SnowizardApplication.class, resourceFilePath("test-snowizard.yml"));
    
    @Before
    public void setUp() {
        final List<String> urls = ImmutableList.of("localhost:" + RULE.getLocalPort());
        client = new SnowizardClient(urls);
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void testClientGetId() throws Exception {
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            client.getId();
        }

        final long endTime = System.currentTimeMillis();
        System.out.println(String.format(
                "generated %d (serially) ids in %d ms", COUNT,
                (endTime - startTime)));
    }

    @Test
    public void testClientGetIds() throws Exception {
        final long startTime = System.currentTimeMillis();
        client.getIds(COUNT);

        final long endTime = System.currentTimeMillis();
        System.out.println(String.format(
                "generated %d (parallel) ids in %d ms", COUNT,
                (endTime - startTime)));
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
