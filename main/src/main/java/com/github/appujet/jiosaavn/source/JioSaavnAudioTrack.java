package com.github.appujet.jiosaavn.source;

import com.github.appujet.jiosaavn.ExtendedAudioSourceManager;
import com.github.appujet.jiosaavn.ExtendedAudioTrack;
import com.github.appujet.jiosaavn.Utils;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.net.URISyntaxException;

public class JioSaavnAudioTrack extends ExtendedAudioTrack {
    private final ExtendedAudioSourceManager manager;

    public JioSaavnAudioTrack(AudioTrackInfo trackInfo, ExtendedAudioSourceManager manager) {
        super(trackInfo, manager);
        this.manager = manager;
    }

    @Override
    public String getPlaybackUrl() throws URISyntaxException {

        var json = manager.fetchJson("/track?id=" + this.trackInfo.identifier);

        if (json.isNull() || json.get("track").isNull()) {
            log.debug("Invalid JSON response or no data found for identifier: {}", this.trackInfo.identifier);
            return null;
        }
        var data = json.get("track");
        var encryptedMediaUrl = data.get("encryptedMediaUrl").text();
        if (encryptedMediaUrl == null) {
            log.debug("Encrypted media URL not found for identifier: {}", this.trackInfo.identifier);
            return null;
        }
        var url = Utils.decryptUrl(encryptedMediaUrl);
        if (data.get("320kbps").asBoolean(false)) {
            url = data.get("url").text().replace("_96.mp4", "_320.mp4");
        }

        return url;
    }

    @Override
    protected long getTrackDuration() {
        return Units.CONTENT_LENGTH_UNKNOWN;
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new JioSaavnAudioTrack(this.trackInfo, manager);
    }
}
