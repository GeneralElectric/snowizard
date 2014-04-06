package com.ge.snowizard.client;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultBackoffStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ge.snowizard.api.protos.SnowizardProtos.SnowizardResponse;
import com.ge.snowizard.client.exceptions.SnowizardClientException;

public class SnowizardClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SnowizardClient.class);
    private static final int MAX_HOSTS = 1024;
    private static final int SOCKET_TIMEOUT_MS = 500;
    private static final int CONNECTION_TIMEOUT_MS = 500;
    private static final int MAX_RETRIES = 3;
    private final Iterable<String> hosts;
    private final CloseableHttpClient client;

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
    public SnowizardClient(final CloseableHttpClient client,
            final Iterable<String> hosts) {
        checkNotNull(client);
        checkNotNull(hosts);

        this.client = client;
        this.hosts = hosts;
    }

    /**
     * Get a new CloseableHttpClient
     * 
     * @return CloseableHttpClient
     */
    public static CloseableHttpClient newHttpClient() {
        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(Boolean.TRUE).setTcpNoDelay(Boolean.TRUE)
                .setSoTimeout(SOCKET_TIMEOUT_MS).build();

        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(MAX_HOSTS);
        manager.setMaxTotal(MAX_HOSTS);
        manager.setDefaultSocketConfig(socketConfig);

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT_MS)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setStaleConnectionCheckEnabled(Boolean.FALSE)
                .setSocketTimeout(SOCKET_TIMEOUT_MS).build();

        final CloseableHttpClient client = HttpClients
                .custom()
                .disableRedirectHandling()
                .setConnectionManager(manager)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionReuseStrategy(
                        new DefaultConnectionReuseStrategy())
                .setConnectionBackoffStrategy(new DefaultBackoffStrategy())
                .setRetryHandler(
                        new DefaultHttpRequestRetryHandler(MAX_RETRIES, false))
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                .build();
        return client;
    }

    /**
     * Return the internal {@link CloseableHttpClient}
     * 
     * @return CloseableHttpClient
     */
    public CloseableHttpClient getHttpClient() {
        return client;
    }

    /**
     * Execute a request to the Snowizard service URL and return a single ID.
     * 
     * @param host
     *            Host:Port pair to connect to
     * @return SnowizardResponse
     * @throws IOException
     *             Error in communicating with Snowizard
     */
    @Nullable
    public SnowizardResponse executeRequest(final String host)
            throws IOException {
        return executeRequest(host, 1);
    }

    /**
     * Execute a request to the Snowizard service URL
     * 
     * @param host
     *            Host:Port pair to connect to
     * @param count
     *            Number of IDs to generate
     * @return SnowizardResponse
     * @throws IOException
     *             Error in communicating with Snowizard
     */
    @Nullable
    public SnowizardResponse executeRequest(final String host, final int count)
            throws IOException {
        final String uri = String.format("http://%s/?count=%d", host, count);
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
     * @return generated ID
     * @throws SnowizardClientException
     *             when unable to get an ID from any host
     */
    public long getId() throws SnowizardClientException {
        for (final String host : hosts) {
            try {
                final SnowizardResponse snowizard = executeRequest(host);
                if (snowizard != null) {
                    return snowizard.getId(0);
                }
            } catch (final Exception ex) {
                LOGGER.warn("Unable to get ID from host ({})", host);
            }
        }
        throw new SnowizardClientException(
                "Unable to generate ID from Snowizard");
    }

    /**
     * Get multiple IDs from Snowizard
     * 
     * @param count
     *            Number of IDs to return
     * @return generated IDs
     * @throws SnowizardClientException
     *             when unable to get an ID from any host
     */
    public List<Long> getIds(final int count) throws SnowizardClientException {
        for (final String host : hosts) {
            try {
                final SnowizardResponse snowizard = executeRequest(host, count);
                if (snowizard != null) {
                    return snowizard.getIdList();
                }
            } catch (final Exception ex) {
                LOGGER.warn("Unable to get ID from host ({})", host);
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
     * {@link CloseableHttpClient}.
     */
    @Override
    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (final IOException e) {
                LOGGER.error("Unable to close HTTP client", e);
            }
        }
    }
}
