package com.github.appujet.jiosaavn;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;


public abstract class ExtendedAudioSourceManager  implements AudioSourceManager, HttpConfigurable {
    public static final Logger log = LoggerFactory.getLogger(ExtendedAudioSourceManager.class);
    protected final HttpInterfaceManager httpInterfaceManager;
    private static final String BASE_API_URL = "https://www.jiosaavn.com/api.php?";
    public ExtendedAudioSourceManager () {
        this(true);
    }

    public ExtendedAudioSourceManager (boolean withoutCookies) {
        this(HttpClientTools.createDefaultThreadLocalManager(), withoutCookies);
    }

    public ExtendedAudioSourceManager (HttpInterfaceManager httpInterfaceManager, boolean withoutCookies) {
        this.httpInterfaceManager = httpInterfaceManager;

        if (withoutCookies) {
            this.httpInterfaceManager.setHttpContextFilter(new Cookies());
        }
    }

    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    public JsonBrowser fetchJson(String endpoint, String[] params, String context) {
        try {
            URI uri = getUri(endpoint, params, context);
            final HttpGet httpGet = new HttpGet(uri);
            try (final CloseableHttpResponse response = this.getHttpInterface().execute(httpGet)) {
                final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                log.info("Response from API: {}", content);
                return JsonBrowser.parse(content);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching JSON from JioSaavn API", e);
        }
    }

    private static URI getUri(String endpoint, String[] params, String context) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(BASE_API_URL);
        uriBuilder.addParameter("__call", endpoint);
        uriBuilder.addParameter("_format", "json");
        uriBuilder.addParameter("_marker", "0");
        uriBuilder.addParameter("api_version", "4");
        uriBuilder.addParameter("ctx", context != null ? context : "web6dot0");

        if (params != null) {
            for (int i = 0; i < params.length; i += 2) {
                if (i + 1 < params.length) {
                    uriBuilder.addParameter(params[i], params[i + 1]);
                }
            }
        }
        return uriBuilder.build();
    }
    
    @Override
    public void shutdown() {
        ExceptionTools.closeWithWarnings(httpInterfaceManager);
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        httpInterfaceManager.configureRequests(configurator);
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        httpInterfaceManager.configureBuilder(configurator);
    }

    public static class Cookies implements HttpContextFilter {
        @Override
        public void onContextOpen(HttpClientContext context) {
            CookieStore cookieStore = context.getCookieStore();

            if (cookieStore == null) {
                cookieStore = new BasicCookieStore();
                context.setCookieStore(cookieStore);
            }
            cookieStore.clear();
        }

        @Override
        public void onContextClose(HttpClientContext context) {
            // Not used
        }

        @Override
        public void onRequest(HttpClientContext context, HttpUriRequest request, boolean isRepetition) {
            // Not used
        }

        @Override
        public boolean onRequestResponse(HttpClientContext context, HttpUriRequest request, HttpResponse response) {
            return false;
        }

        @Override
        public boolean onRequestException(HttpClientContext context, HttpUriRequest request, Throwable error) {
            return false;
        }
    }
}
