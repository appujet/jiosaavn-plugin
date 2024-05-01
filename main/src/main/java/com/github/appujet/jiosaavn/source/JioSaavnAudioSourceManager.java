package com.github.appujet.jiosaavn.source;

import com.github.appujet.jiosaavn.ExtendedAudioPlaylist;
import com.github.appujet.jiosaavn.ExtendedAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JioSaavnAudioSourceManager extends ExtendedAudioSourceManager {
    private static final Pattern JIOSAAVN_REGEX = Pattern.compile("(https?://)(www\\.)?jiosaavn\\.com/(song|album|featured|artist)/([a-zA-Z0-9-_]+)");
    public static String BASE_API = null;
    public static final String SEARCH_PREFIX = "jssearch:";
    public static final String RECOMMENDATIONS_PREFIX = "jsrec:";

    public JioSaavnAudioSourceManager(String apiURL) {
        if (apiURL == null || apiURL.isEmpty()) {
            throw new IllegalArgumentException("API URL must be provided");
        }
        BASE_API = apiURL;
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
    public void encodeTrack(AudioTrack audioTrack, DataOutput dataOutput) {
// No need for encoding, just pass the track info without serialization
        JioSaavnAudioTrack jioSaavnAudioTrack = (JioSaavnAudioTrack) audioTrack;
        AudioTrackInfo trackInfo = jioSaavnAudioTrack.getInfo();
        try {
            // Write track info properties individually
            dataOutput.writeUTF(trackInfo.title);
            dataOutput.writeUTF(trackInfo.author);
            dataOutput.writeLong(trackInfo.length);
            dataOutput.writeUTF(trackInfo.identifier);
            dataOutput.writeBoolean(trackInfo.isStream);
            dataOutput.writeUTF(trackInfo.uri);
            dataOutput.writeUTF(trackInfo.artworkUrl);
            // If there are additional properties, write them here
        } catch (IOException e) {
            throw new RuntimeException("Error encoding track", e);
        }
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo audioTrackInfo, DataInput dataInput) {
        return new JioSaavnAudioTrack(audioTrackInfo, this);
    }

    private AudioItem getSearchResult(String query) throws IOException {
        final JsonBrowser json = this.fetchJson("/search?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        if (json.isNull() || json.get("data").isNull()) {
            return AudioReference.NO_TRACK;
        }
        final JsonBrowser data = json.get("data");
        if (data.get("songs").isNull()) {
            return AudioReference.NO_TRACK;
        }
        final JsonBrowser song = data.get("songs").get("results").index(0);
        if (song.isNull()) {
            return AudioReference.NO_TRACK;
        }
        var tracks = this.fetchJson("/songs?ids=" + song.get("id").text());
        return this.buildTrack(tracks.get("data").index(0));
    }

    private AudioItem getTrack(String identifier) throws IOException {
        final JsonBrowser json = this.fetchJson("/songs?link=" + identifier);
        if (json.isNull() || json.get("data").isNull()) {
            return AudioReference.NO_TRACK;
        }
        final JsonBrowser data = json.get("data").index(0);
        return this.buildTrack(data);
    }

    public AudioItem getAlbum(String identifier) {

        final JsonBrowser json = this.fetchJson("/albums?link=" + identifier);

        if (json.isNull() || json.get("data").isNull()) {
            return AudioReference.NO_TRACK;
        }
        final JsonBrowser data = json.get("data");
        if (data.get("songs").isNull()) {
            return AudioReference.NO_TRACK;
        }
        return new JioSaavnAudioPlaylist(
                data.get("name").text(),
                this.buildTracks(data.get("songs")),
                ExtendedAudioPlaylist.Type.ALBUM,
                data.get("url").text(),
                this.parseImage(data.get("image")),
                this.parseArtist(data),
                (int) data.get("songCount").asLong(0)
        );
    }

    public AudioItem getPlaylist(String identifier) {
        final JsonBrowser json = this.fetchJson("/playlists?link=" + identifier);
        if (json.isNull() || json.get("data").isNull()) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser data = json.get("data");
        if (data.get("songs").isNull()) {
            return AudioReference.NO_TRACK;
        }
        return new JioSaavnAudioPlaylist(
                data.get("name").text(),
                this.buildTracks(data.get("songs")),
                ExtendedAudioPlaylist.Type.PLAYLIST,
                data.get("url").text(),
                this.parseImage(data.get("image")),
                this.parseArtist(data),
                (int) data.get("songCount").asLong(0)
        );
    }

    private AudioItem getArtist(String identifier) {
        final JsonBrowser json = this.fetchJson("/artists?link=" + identifier);
        if (json.isNull() || json.get("data").isNull()) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser data = json.get("data");
        if (data.get("topSongs").isNull()) {
            return AudioReference.NO_TRACK;
        }
        return new JioSaavnAudioPlaylist(
                data.get("name").text(),
                this.buildTracks(data.get("topSongs")),
                ExtendedAudioPlaylist.Type.ARTIST,
                data.get("url").text(),
                this.parseImage(data.get("image")),
                data.get("name").text(),
                null
        );
    }

    public AudioItem getRecommendations(String identifier) {
        final JsonBrowser json = this.fetchJson("/songs/" + identifier + "/suggestions?limit=10");
        if (json.isNull() || json.get("data").isNull()) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser data = json.get("data");
        if (data.isNull()) {
            return AudioReference.NO_TRACK;
        }

        var tracks = this.buildTracks(data);

        return new JioSaavnAudioPlaylist(
                "Recommendations",
                tracks,
                ExtendedAudioPlaylist.Type.RECOMMENDATIONS,
                null,
                null,
                null,
                null
        );
    }

    public JsonBrowser fetchJson(String pageURl) {
        final HttpGet httpGet = new HttpGet(BASE_API + pageURl);
        try (final CloseableHttpResponse response = this.getHttpInterface().execute(httpGet)) {
            final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            return JsonBrowser.parse(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<AudioTrack> buildTracks(JsonBrowser json) {
        var tracks = new ArrayList<AudioTrack>();
        for (var track : json.values()) {
            var parsedTrack = this.buildTrack(track);
            if (parsedTrack != null) {
                tracks.add(parsedTrack);
            }
        }
        return tracks;
    }

    private AudioTrack buildTrack(JsonBrowser data) {
        if (data.isNull()) {
            return null;
        }
        final String title = cleanString(data.get("name").text());
        final String id = data.get("id").text();
        final String artwork = this.parseImage(data.get("image"));
        final long duration = data.get("duration").asLong(1) * 1000;
        final String url = data.get("url").text();
        var artist = cleanString(this.parseArtist(data));
        return new JioSaavnAudioTrack(
                new AudioTrackInfo(
                        title,
                        artist,
                        duration,
                        id,
                        false,
                        url,
                        artwork,
                        null
                ),
                this
        );
    }

    private String parseArtist(JsonBrowser json) {
        if (json.isNull()) {
            return null;
        }
        var artists = json.get("artists").get("primary");
        if (artists.isNull()) {
            return "Unknown";
        }
        return artists.values().stream().map(name -> name.get("name").text()).collect(Collectors.joining(", "));
    }

    private String cleanString(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("\"", "")
                .replace("&quot;", "")
                .replace("&amp;", "");
    }

    private String parseImage(JsonBrowser json) {
        if (json.isNull()) {
            return null;
        }
        var image = json.index(2);
        if (image.isNull()) {
            image = json.index(1);
        }
        if (image.isNull()) {
            image = json.index(0);
        }
        if (image.isNull()) {
            return null;
        }
        return image.get("url").text();
    }
}
