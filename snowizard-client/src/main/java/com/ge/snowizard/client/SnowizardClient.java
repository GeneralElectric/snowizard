package com.ge.snowizard.client;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardResponse;
import com.ge.snowizard.client.exceptions.SnowizardClientException;

public class SnowizardClient {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_INTERVAL = 10;
    private final Iterable<String> hosts;
    private final AutoRetryHttpClient client;

    /**
     * Constructor
     *
     * @param hosts
     */
    public SnowizardClient(final Iterable<String> hosts) {
        this(newHttpClient(), hosts);
    }

    /**
     * Constructor
     *
     * @param client
     *            {@link HttpClient} to use
     * @param hosts
     *            List of hosts to connect to
     */
    public SnowizardClient(final HttpClient client, final Iterable<String> hosts) {
        if (client == null) {
            throw new NullPointerException("client cannot be null");
        }
        if (hosts == null) {
            throw new NullPointerException("hosts cannot be null");
        }

        this.client = new AutoRetryHttpClient(client,
                new DefaultServiceUnavailableRetryStrategy(MAX_RETRIES,
                        RETRY_INTERVAL));
        this.hosts = hosts;
    }

    /**
     * Get a new HttpClient
     *
     * @return AutoRetryHttpClient
     */
    public static HttpClient newHttpClient() {
        final PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
        manager.setDefaultMaxPerRoute(1024);
        manager.setMaxTotal(1024);

        final BasicHttpParams params = new BasicHttpParams();
        params.setParameter(AllClientPNames.COOKIE_POLICY,
                CookiePolicy.IGNORE_COOKIES);
        params.setParameter(AllClientPNames.SO_TIMEOUT, 500);
        params.setParameter(AllClientPNames.CONNECTION_TIMEOUT, 500);

        params.setParameter(AllClientPNames.TCP_NODELAY, Boolean.TRUE);
        params.setParameter(AllClientPNames.STALE_CONNECTION_CHECK,
                Boolean.FALSE);

        final DefaultHttpClient client = new DefaultHttpClient(manager, params);
        client.setReuseStrategy(new DefaultConnectionReuseStrategy());
        client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(
                MAX_RETRIES, false));
        return new AutoRetryHttpClient(new DecompressingHttpClient(client),
                new DefaultServiceUnavailableRetryStrategy(MAX_RETRIES,
                        RETRY_INTERVAL));
    }

    /**
     * Return the internal {@link HttpClient}
     *
     * @return HttpClient
     */
    public HttpClient getHttpClient() {
        return client;
    }

    /**
     * Execute a request to the Snowizard service URL
     *
     * @param host
     * @return SnowizardProtos.SnowizardResponse
     * @throws IOException
     * @throws ClientProtocolException
     */
    public SnowizardResponse executeRequest(final String host)
            throws IOException, ClientProtocolException {
        final String uri = String.format("http://%s/", host);
        final HttpGet request = new HttpGet(uri);
        request.addHeader(HttpHeaders.ACCEPT, "application/x-protobuf");
        request.addHeader(HttpHeaders.USER_AGENT, getUserAgent());

        SnowizardResponse snowizard = null;
        try {
            final BasicHttpContext context = new BasicHttpContext();
            final HttpResponse response = client.execute(request, context);
            final int code = response.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    snowizard = SnowizardResponse
                            .parseFrom(entity.getContent());
                }
                EntityUtils.consumeQuietly(entity);
            }
        } finally {
            request.releaseConnection();
        }
        return snowizard;
    }

    /**
     * Get a new ID from Snowizard
     *
     * @return long
     * @throws SnowizardClientException
     */
    public long getId() throws SnowizardClientException {
        SnowizardResponse snowizard = null;
        for (String host : hosts) {
            try {
                snowizard = executeRequest(host);
                if (snowizard != null) {
                    return snowizard.getId();
                }
            } catch (Exception ex) {
            }
        }
        throw new SnowizardClientException(
                "Unable to generate ID from Snowizard");
    }

    /**
     * Get the user-agent for the client
     *
     * @return
     */
    public String getUserAgent() {
        return String.format("SnowizardClient/%s", getClass().getPackage()
                .getImplementationVersion());
    }

    /**
     * Closes the underlying connection pool used by the internal
     * {@link HttpClient}.
     */
    public void close() {
        if (client != null) {
            client.getConnectionManager().shutdown();
        }
    }
}
