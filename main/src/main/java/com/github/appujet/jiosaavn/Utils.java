package com.github.appujet.jiosaavn;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String KEY = "38346591";
    private static final String ALGORITHM = "DES/ECB/NoPadding";
    private static final String[] QUALITIES = { "_320", "_160", "_96", "_48", "_12" };

    // Regular expressions for extracting tokens
    private static final Pattern SONG_PATTERN = Pattern.compile("jiosaavn\\.com/song/[^/]+/([^/]+)$");
    private static final Pattern ARTIST_PATTERN = Pattern.compile("jiosaavn\\.com/artist/[^/]+/([^/]+)$");
    private static final Pattern ALBUM_PATTERN = Pattern.compile("jiosaavn\\.com/album/[^/]+/([^/]+)$");
    private static final Pattern PLAYLIST_PATTERN = Pattern.compile("jiosaavn\\.com/(?:featured|s/playlist)/[^/]+/([^/]+)$");

    /**
     * Extracts a song token from a JioSaavn song URL.
     *
     * @param url The song URL.
     * @return The song token or null if not found.
     */
    public static String extractSongToken(String url) {
        return extractToken(url, SONG_PATTERN);
    }

    /**
     * Extracts an artist token from a JioSaavn artist URL.
     *
     * @param url The artist URL.
     * @return The artist token or null if not found.
     */
    public static String extractArtistToken(String url) {
        return extractToken(url, ARTIST_PATTERN);
    }

    /**
     * Extracts an album token from a JioSaavn album URL.
     *
     * @param url The album URL.
     * @return The album token or null if not found.
     */
    public static String extractAlbumToken(String url) {
        return extractToken(url, ALBUM_PATTERN);
    }

    /**
     * Extracts a playlist token from a JioSaavn playlist URL.
     *
     * @param url The playlist URL.
     * @return The playlist token or null if not found.
     */
    public static String extractPlaylistToken(String url) {
        return extractToken(url, PLAYLIST_PATTERN);
    }

    /**
     * Extracts a token using the provided pattern.
     *
     * @param url The URL to extract the token from.
     * @param pattern The regex pattern to match the token.
     * @return The token if found, or null if not.
     */
    private static String extractToken(String url, Pattern pattern) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);  // Return the first captured group
        }
        return null;
    }
    public static JsonBrowser fetchJson(String pageURl, ExtendedAudioSourceManager sourceManager) {
        final HttpGet httpGet = new HttpGet(pageURl);
        try (final CloseableHttpResponse response = sourceManager.getHttpInterface().execute(httpGet)) {
            final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            return JsonBrowser.parse(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Generates a download link by decrypting the encrypted media URL.
     *
     * @param encryptedMediaUrl The encrypted media URL.
     * @return The download link or null if not valid.
     */
    public static String getDownloadLink(String encryptedMediaUrl) {
        if (encryptedMediaUrl == null || encryptedMediaUrl.isEmpty())
            return null;

        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMediaUrl);
            Key key = new SecretKeySpec(KEY.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedLink = new String(decryptedBytes).trim();
            System.out.println("Decrypted Link: " + decryptedLink);
            for (String quality : QUALITIES) {
                String downloadUrl = decryptedLink.replace("_96", quality);
                if (isValidUrl(downloadUrl)) {
                    return downloadUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to check if a URL is valid
    private static boolean isValidUrl(String url) {
        return url.startsWith("https://aac.saavncdn.com/") && url.endsWith(".mp4");
    }
}
