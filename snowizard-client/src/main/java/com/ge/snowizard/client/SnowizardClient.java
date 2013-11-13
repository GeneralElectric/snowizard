package com.ge.snowizard.client;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.ByteArrayEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardResponse;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardRequest;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardBatchResponse;
import com.ge.snowizard.client.exceptions.SnowizardClientException;

public class SnowizardClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SnowizardClient.class);
    private static final int MAX_HOSTS = 1024;
    private static final int SOCKET_TIMEOUT_MS = 500;
    private static final int CONNECTION_TIMEOUT_MS = 500;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_INTERVAL = 10;
    private final Iterable<String> hosts;
    private final HttpClient client;

    /**
     * Constructor
     *
     * @param hosts
     *            List of host:port pairs to connect to
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
     *            List of host:port pairs to connect to
     */
    public SnowizardClient(final HttpClient client, final Iterable<String> hosts) {
        checkNotNull(client);
        checkNotNull(hosts);

        this.client = client;
        this.hosts = hosts;
    }

    /**
     * Get a new HttpClient
     *
     * @return AutoRetryHttpClient
     */
    public static HttpClient newHttpClient() {
        final PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
        manager.setDefaultMaxPerRoute(MAX_HOSTS);
        manager.setMaxTotal(MAX_HOSTS);

        final BasicHttpParams params = new BasicHttpParams();
        params.setParameter(AllClientPNames.COOKIE_POLICY,
                CookiePolicy.IGNORE_COOKIES);
        params.setParameter(AllClientPNames.SO_TIMEOUT, SOCKET_TIMEOUT_MS);
        params.setParameter(AllClientPNames.CONNECTION_TIMEOUT,
                CONNECTION_TIMEOUT_MS);

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
     *            Host:Port pair to connect to
     * @return SnowizardResponse
     * @throws IOException
     *             Error in communicating with Snowizard
     */
    public SnowizardResponse executeRequest(final String host)
            throws IOException {
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

    public SnowizardBatchResponse executePostRequest(final String host, byte[] payload)
            throws IOException {
        final String uri = String.format("http://%s/", host);
        final HttpPost request = new HttpPost(uri);

        request.setEntity(new ByteArrayEntity(payload));
        request.setHeader("Content-Type", "application/x-protobuf");
        request.addHeader(HttpHeaders.ACCEPT, "application/x-protobuf");
        request.addHeader(HttpHeaders.USER_AGENT, getUserAgent());

        SnowizardBatchResponse snowizard = null;
        try {
            final BasicHttpContext context = new BasicHttpContext();
            final HttpResponse response = client.execute(request, context);
            final int code = response.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    snowizard = SnowizardBatchResponse
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
     * @return generated ID
     * @throws SnowizardClientException
     *             when unable to get an ID from any host
     */
    public long getId() throws SnowizardClientException {
        for (String host : hosts) {
            try {
                final SnowizardResponse snowizard = executeRequest(host);
                if (snowizard != null) {
                    return snowizard.getId();
                }
            } catch (Exception ex) {
                LOGGER.warn("Unable to get ID from host ({})", host);
            }
        }
        throw new SnowizardClientException(
                "Unable to generate ID from Snowizard");
    }

    /**
     * Get a new ID from Snowizard
     *
     * @param n number of IDs to generate
     * @return generated IDs
     * @throws SnowizardClientException
     */
    public List<Long> getIds(int n) throws SnowizardClientException {

        SnowizardRequest req = SnowizardRequest.newBuilder().setBatchSize(n).build();

        for (String host : hosts) {
            try {
                final SnowizardBatchResponse response = executePostRequest(host, req.toByteArray());
                if (response != null) {
                    return response.getIdsList();
                }
            } catch (Exception ignore) {
                // ignore the exception and try the next host
            }
        }
        throw new SnowizardClientException(
                "Unable to generate batch of IDs from Snowizard");
    }

    /**
     * Get the user-agent for the client
     *
     * @return user-agent for the client
     */
    public static String getUserAgent() {
        return "snowizard-client";
    }

    /**
     * Closes the underlying connection pool used by the internal
     * {@link HttpClient}.
     */
    @Override
    public void close() {
        if (client != null) {
            client.getConnectionManager().shutdown();
        }
    }
}
