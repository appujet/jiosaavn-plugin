package com.github.appujet.jiosaavn.source;

import com.github.appujet.jiosaavn.ExtendedAudioSourceManager;
import com.github.appujet.jiosaavn.ExtendedAudioTrack;
import com.github.appujet.jiosaavn.Utils;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class JioSaavnAudioTrack extends ExtendedAudioTrack {
    private final ExtendedAudioSourceManager manager;

    public JioSaavnAudioTrack(AudioTrackInfo trackInfo, ExtendedAudioSourceManager manager) {
        super(trackInfo, manager);
        this.manager = manager;
    }

    @Override
    public String getPlaybackUrl() {

        var json = manager.fetchJson("/api/track?id=" + this.trackInfo.identifier);

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
        var url = Utils.getDownloadLink(encryptedMediaUrl);
        
        if (url == null) {
            log.debug("Failed to decrypt media URL for identifier: {}", this.trackInfo.identifier);
            return null;
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
