package com.ge.snowizard.client.integration;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.ge.snowizard.client.SnowizardClient;
import com.google.common.collect.ImmutableList;

public class SnowizardClientIT {

    private SnowizardClient client;

    @Before
    public void setUp() {
        final List<String> urls = ImmutableList.of("127.0.0.1:8080");
        client = new SnowizardClient(urls);
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void testClientGetId() throws Exception {
        final int count = 1000;
        for (int i = 0; i < count; i++) {
            client.getId();
        }
    }

    @Test
    public void testClientGetIds() throws Exception {
        final int count = 1000;

        final long startTime = System.currentTimeMillis();
        client.getIds(count);

        final long endTime = System.currentTimeMillis();
        System.out.println(String.format(
                "generated %d (batch) ids in %d ms", count,
                (endTime - startTime)));
    }
}
