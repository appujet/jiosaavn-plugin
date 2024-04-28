package com.github.appujet.jiosaavn;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Utils {

    public static JsonBrowser fetchJson(String pageURl, ExtendedAudioSourceManager sourceManager) {
        final HttpGet httpGet = new HttpGet(pageURl);
        try (final CloseableHttpResponse response = sourceManager.getHttpInterface().execute(httpGet)) {
            final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            return JsonBrowser.parse(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}