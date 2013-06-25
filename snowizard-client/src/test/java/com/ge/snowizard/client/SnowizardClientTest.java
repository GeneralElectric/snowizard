package com.ge.snowizard.client;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ge.snowizard.client.SnowizardClient;

public class SnowizardClientTest {

    private final List<String> urls = new ArrayList<String>();

    @Before
    public void setUp() {
        urls.add("http://127.0.0.1:8069");
    }
    
    @After
    public void tearDown() {
        urls.clear();
    }

    @Test
    @Ignore
    public void testClientGetId() throws Exception {
        final SnowizardClient client = new SnowizardClient(urls);

        final int count = 1000;

        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            //System.out.println(client.getId());
            client.getId();
        }

        final long endTime = System.currentTimeMillis();
        System.out.println(String.format(
                "generated %d ids in %d ms", count,
                (endTime - startTime)));
    }
}
