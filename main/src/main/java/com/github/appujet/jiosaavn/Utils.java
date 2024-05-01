package com.github.appujet.jiosaavn;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;

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
    private static Iterable<Character> cycle(String i) {
        return () -> new Iterator<>() {
            private int index = -1;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Character next() {
                index = (index + 1) % i.length();
                return i.charAt(index);
            }
        };
    }

    private static String decryptXor(String key, String cipher) {
        StringBuilder decrypted = new StringBuilder();
        Iterator<Character> keyIterator = cycle(key).iterator();
        for (char ch : cipher.toCharArray()) {
            if (!keyIterator.hasNext())
                keyIterator = cycle(key).iterator(); // restart key iterator if it reaches the end
            char k = keyIterator.next();
            decrypted.append((char) (ch ^ k));
        }
        return decrypted.toString();
    }

    public static String decryptUrl(String key, String url) {
        String xorUrl = new String(Base64.getDecoder().decode(url));
        return decryptXor(key, xorUrl);
    }
}