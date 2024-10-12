package com.github.appujet.jiosaavn.source;

import com.github.appujet.jiosaavn.ExtendedAudioPlaylist;
import com.github.appujet.jiosaavn.ExtendedAudioSourceManager;
import com.github.appujet.jiosaavn.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
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

    public JioSaavnAudioSourceManager(int playlistTrackLimit, int recommendationsTrackLimit) {
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
        final JsonBrowser json = this.fetchJson("search.getResults",
                new String[] { "q", query, "cc", "in", "includeMetaTags", "1", "n", "100" }, null);

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
        String token = Utils.extractSongToken(url);
        final JsonBrowser json = this.fetchJson("webapi.get", new String[] { "token", token, "type", "song" }, null);
        if (json.isNull() || json.get("songs").isNull() || json.get("songs").values().isEmpty()) {
            return AudioReference.NO_TRACK;
        }
        final JsonBrowser data = json.get("songs").index(0);
        return this.buildTrack(data);
    }

    public AudioItem getAlbum(String url) {
        String token = Utils.extractAlbumToken(url);
        final JsonBrowser json = this.fetchJson("webapi.get", new String[] { "type", "album", "token", token }, null);

        if (json.isNull() || json.get("list").isNull()) {
            return AudioReference.NO_TRACK;
        }
        final JsonBrowser data = json.get("list");
        if (data.isNull() || !data.isList()) {
            return AudioReference.NO_TRACK;
        }
        final String albumTitle = cleanString(json.get("title").text());
        final List<AudioTrack> tracks = data.values().stream()
                .map(this::buildTrack)
                .collect(Collectors.toList());
        final String albumUrl = json.get("perma_url").text();
        final String artwork = this.parseImage(json.get("image"));
        final String artist = parseArtist(json);
        final int trackCount = (int) json.get("list_count").asLong(0);
        return new JioSaavnAudioPlaylist(albumTitle, tracks, ExtendedAudioPlaylist.Type.ALBUM, albumUrl, artwork,
                artist, trackCount);
    }

    private AudioItem getArtist(String url) {
        String token = Utils.extractArtistToken(url);
        final JsonBrowser json = this.fetchJson("webapi.get", new String[] { "type", "artist", "token", token }, null);
        if (json.isNull() || json.get("topSongs").isNull()) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser data = json.get("topSongs");
        if (data.isNull() || !data.isList()) {
            return AudioReference.NO_TRACK;
        }
        final String artistName = cleanString(json.get("name").text());
        final List<AudioTrack> tracks = data.values().stream()
                .map(this::buildTrack)
                .collect(Collectors.toList());

        final String artwork = this.parseImage(json.get("image"));
        final String artist = parseArtist(json);

        return new JioSaavnAudioPlaylist(
                artistName,
                tracks,
                ExtendedAudioPlaylist.Type.ARTIST,
                url,
                artwork,
                artist,
                null);

    }

    public AudioItem getPlaylist(String identifier) {
        String token = Utils.extractPlaylistToken(identifier);
        final JsonBrowser json = this.fetchJson("webapi.get",
                new String[] { "type", "playlist", "token", token, "n", String.valueOf(playlistTrackLimit) }, null);
        if (json.isNull() || json.get("list").isNull()) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser data = json.get("list");
        if (data.isNull() || !data.isList()) {
            return AudioReference.NO_TRACK;
        }
        final String playlistTitle = cleanString(json.get("title").text());
        final List<AudioTrack> tracks = data.values().stream()
                .map(this::buildTrack)
                .collect(Collectors.toList());
        final String playlistUrl = json.get("perma_url").text();
        final String artwork = this.parseImage(json.get("image"));
        final String artist = parseArtist(json);
        final int trackCount = (int) json.get("list_count").asLong(0);

        return new JioSaavnAudioPlaylist(playlistTitle, tracks, ExtendedAudioPlaylist.Type.PLAYLIST, playlistUrl,
                artwork, artist, trackCount);
    }

    public AudioItem getRecommendations(String identifier) {
        String stationId = getStationId(identifier);

        if (stationId == null) {
            log.error("No station ID found for identifier: {}", identifier);
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser json = this.fetchJson("webradio.getSong",
                new String[] { "stationid", stationId, "k", String.valueOf(recommendationsTrackLimit) }, "android");

        if (json.isNull()) {
            log.error("No new song found: {}", json.get("error").text());
            return AudioReference.NO_TRACK;
        }

        final List<AudioTrack> tracks = new ArrayList<>();

        for (int i = 0; i < json.values().size(); i++) {
            JsonBrowser songData = json.get(String.valueOf(i)).get("song");
            if (!songData.isNull()) {
                AudioTrack track = buildTrack(songData);
                tracks.add(track);
            }
        }

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

    private String getStationId(String identifier) {
        String encodedSongId = "[\"" + Utils.encodeURIComponent(identifier) + "\"]";
        final JsonBrowser json = this.fetchJson("webradio.createEntityStation",
                new String[] { "entity_id", encodedSongId, "entity_type", "queue" }, "android");

        if (json.isNull() || json.get("stationid").isNull()) {
            return null;
        }
        return json.get("stationid").text();
    }

    private AudioTrack buildTrack(JsonBrowser data) {
        if (data.isNull()) {
            return null;
        }

        final String title = cleanString(data.get("title").text());
        final String id = data.get("id").text();
        final String artwork = this.parseImage(data.get("image"));
        final long duration = data.get("more_info").get("duration").asLong(1) * 1000;
        final String url = data.get("perma_url").text();
        final String artist = parseArtist(data);

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

    private String parseArtist(JsonBrowser json) {
        if (json.isNull()) {
            return null;
        }
        JsonBrowser artistMap = json.get("more_info").get("artistMap");
        if (artistMap.isNull()) {
            return "Unknown";
        }
        JsonBrowser primaryArtists = artistMap.get("primary_artists");
        if (primaryArtists.isNull()) {
            return "Unknown";
        }
        if (primaryArtists.isList()) {
            if (!primaryArtists.values().isEmpty()) {
                return primaryArtists.values().iterator().next().get("name").text();
            }
        }
        return "Unknown";
    }

    private String parseImage(JsonBrowser json) {
        if (json.isNull()) {
            return null;
        }
        String imageUrl = json.text();
        if (imageUrl.contains("150x150")) {
            imageUrl = imageUrl.replace("150x150", "500x500");
        }
        return imageUrl;
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
