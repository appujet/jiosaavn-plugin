package com.github.appujet.jiosaavn;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.function.Consumer;
import java.util.function.Function;


public abstract class ExtendedAudioSourceManager  implements AudioSourceManager, HttpConfigurable {
    protected final HttpInterfaceManager httpInterfaceManager;

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

            // Reset cookies for each sequence of requests.
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
