package com.ge.snowizard.client.integration;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ge.snowizard.client.SnowizardClient;

public class SnowizardClientIT {

    private SnowizardClient client;

    @Before
    public void setUp() {
        final List<String> urls = new ArrayList<String>();
        urls.add("http://127.0.0.1:8069");
        client = new SnowizardClient(urls);
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void testClientGetId() throws Exception {
        final int count = 1000;

        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            client.getId();
        }

        final long endTime = System.currentTimeMillis();
        System.out.println(String.format(
                "generated %d ids in %d ms", count,
                (endTime - startTime)));
    }
}
