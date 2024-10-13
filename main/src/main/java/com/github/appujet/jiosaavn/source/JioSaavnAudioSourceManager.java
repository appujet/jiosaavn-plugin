package com.github.appujet.jiosaavn.source;

import com.github.appujet.jiosaavn.ExtendedAudioPlaylist;
import com.github.appujet.jiosaavn.ExtendedAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JioSaavnAudioSourceManager extends ExtendedAudioSourceManager {
    private static final Pattern JIOSAAVN_REGEX = Pattern.compile(
            "(https?://)(www\\.)?jiosaavn\\.com/(song|album|featured|artist|s/playlist)/([a-zA-Z0-9-_]+)(/([a-zA-Z0-9-_]+))?");
    private final int playlistTrackLimit;
    private final int recommendationsTrackLimit;
    public static final String SEARCH_PREFIX = "jssearch:";
    public static final String RECOMMENDATIONS_PREFIX = "jsrec:";

    public JioSaavnAudioSourceManager(String apiURL, int playlistTrackLimit, int recommendationsTrackLimit) {
        BASE_API_URL = apiURL;
        this.playlistTrackLimit = Math.abs(playlistTrackLimit);
        this.recommendationsTrackLimit = Math.abs(recommendationsTrackLimit);
    }

    @Override
    public String getSourceName() {
        return "jiosaavn";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager audioPlayerManager, AudioReference audioReference) {

        try {
            if (audioReference.identifier.startsWith(SEARCH_PREFIX)) {
                return this.getSearchResult(audioReference.identifier.substring(SEARCH_PREFIX.length()));
            }
            if (audioReference.identifier.startsWith(RECOMMENDATIONS_PREFIX)) {
                return this.getRecommendations(audioReference.identifier.substring(RECOMMENDATIONS_PREFIX.length()));
            }
            var matcher = JIOSAAVN_REGEX.matcher(audioReference.identifier);
            if (!matcher.find()) {
                return null;
            }
            String type = matcher.group(3);

            switch (type) {
                case "song":
                    return this.getTrack(audioReference.identifier);
                case "album":
                    return this.getAlbum(audioReference.identifier);
                case "featured":
                case "s/playlist":
                    return this.getPlaylist(audioReference.identifier);
                case "artist":
                    return this.getArtist(audioReference.identifier);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack audioTrack) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack audioTrack, DataOutput dataOutput) throws IOException {
        
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo audioTrackInfo, DataInput dataInput) {
        return new JioSaavnAudioTrack(audioTrackInfo, this);
    }

    private AudioItem getSearchResult(String query) throws IOException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        final JsonBrowser json = this.fetchJson("/search?q=" + encodedQuery);

        if (json.isNull()) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser songs = json.get("results");

        if (songs.isNull() || !songs.isList()) {
            return AudioReference.NO_TRACK;
        }

        final List<AudioTrack> tracks = songs.values().stream()
                .map(this::buildTrack)
                .collect(Collectors.toList());

        return new BasicAudioPlaylist(
                "Search results for: " + query,
                tracks,
                null,
                true);
    }

    private AudioItem getTrack(String url) throws IOException {
        final JsonBrowser json = this.fetchJson("/track?url=" + url);

        if (json.isNull() || json.get("track").isNull()) {
            return AudioReference.NO_TRACK;
        }
        final JsonBrowser data = json.get("track");
        return this.buildTrack(data);
    }

    public AudioItem getAlbum(String url) {
        final JsonBrowser json = this.fetchJson("/album?url=" + url);
        if (json.isNull() || json.get("album").isNull()) {
            return AudioReference.NO_TRACK;
        }
        final JsonBrowser data = json.get("album");

        if (data.isNull() || !data.get("tracks").isList()) {
            return AudioReference.NO_TRACK;
        }
        final String albumTitle = cleanString(data.get("name").text());
        final List<AudioTrack> tracks = data.get("tracks").values().stream()
                .map(this::buildTrack)
                .collect(Collectors.toList());
        final String albumUrl = data.get("url").text();
        final String artwork = data.get("artworkUrl").text();
        
        final int trackCount = (int) data.get("totalSongs").asLong(0);
        return new JioSaavnAudioPlaylist(albumTitle, tracks, ExtendedAudioPlaylist.Type.ALBUM, albumUrl, artwork,
                null, trackCount);
    }

    private AudioItem getArtist(String url) {

        final JsonBrowser json = this.fetchJson("/artist?url=" + url);
        if (json.isNull() || json.get("artist").isNull()) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser data = json.get("artist");
        if (data.isNull() || !data.get("tracks").isList()) {
            return AudioReference.NO_TRACK;
        }
        final String artistName = cleanString(data.get("name").text());
        final List<AudioTrack> tracks = data.get("tracks").values().stream()
                .map(this::buildTrack)
                .collect(Collectors.toList());

        final String artwork = data.get("artworkUrl").text();

        return new JioSaavnAudioPlaylist(
                artistName,
                tracks,
                ExtendedAudioPlaylist.Type.ARTIST,
                url,
                artwork,
                artistName,
                null);

    }

    public AudioItem getPlaylist(String identifier) {

        final JsonBrowser json = this.fetchJson("/playlist?url=" + identifier + "&limit=" + playlistTrackLimit);
        if (json.isNull() || json.get("playlist").isNull()) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser data = json.get("playlist");
        if (data.isNull() || !data.get("tracks").isList()) {
            return AudioReference.NO_TRACK;
        }
        final String playlistTitle = cleanString(data.get("title").text());
        final List<AudioTrack> tracks = data.get("tracks").values().stream()
                .map(this::buildTrack)
                .collect(Collectors.toList());
        final String playlistUrl = data.get("uri").text();
        final String artwork = data.get("artworkUrl").text();

        final int trackCount = (int) data.get("totalSongs").asLong(0);

        return new JioSaavnAudioPlaylist(playlistTitle, tracks, ExtendedAudioPlaylist.Type.PLAYLIST, playlistUrl,
                artwork, null, trackCount);
    }

    public AudioItem getRecommendations(String identifier) {

        final JsonBrowser json = this.fetchJson("/recommendations?id=" + identifier + "&limit=" + recommendationsTrackLimit);

        if (json.isNull() || !json.get("tracks").isList()) {
            return AudioReference.NO_TRACK;
        }

        final List<AudioTrack> tracks = json.get("tracks").values().stream()
                .map(this::buildTrack)
                .collect(Collectors.toList());

        if (tracks.isEmpty()) {
            return AudioReference.NO_TRACK;
        }

        return new JioSaavnAudioPlaylist(
                "Recommendations",
                tracks,
                ExtendedAudioPlaylist.Type.RECOMMENDATIONS,
                null,
                null,
                null,
                null);
    }

    private AudioTrack buildTrack(JsonBrowser data) {
        if (data.isNull()) {
            return null;
        }
        final String title = cleanString(data.get("title").text());
        final String id = data.get("identifier").text();
        final String artwork = data.get("artworkUrl").text();
        final long duration = data.get("length").asLong(1);
        final String url = data.get("uri").text();
        final String artist = cleanString(data.get("author").text());

        return new JioSaavnAudioTrack(
                new AudioTrackInfo(
                        title,
                        artist,
                        duration,
                        id,
                        false,
                        url,
                        artwork,
                        null),
                this);
    }

    private String cleanString(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("\"", "")
                .replace("&quot;", "")
                .replace("&amp;", "");
    }
}
