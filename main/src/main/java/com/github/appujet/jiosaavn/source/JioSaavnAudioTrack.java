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
        return getDownloadURL(this.trackInfo.identifier);
    }

    private String getDownloadURL(String identifier) {
        // Fetch JSON data from the API
        var json = Utils.fetchJson(JioSaavnAudioSourceManager.BASE_API + "/songs?ids=" + identifier, manager);

        // Extract the download URL information
        var downloadInfoLink = json.get("data").index(0).get("downloadUrl");

        if (downloadInfoLink.isNull()) {
            return null;
        }

        // Retrieve all download URLs
        var downloadUrls = downloadInfoLink.values();
        if (downloadUrls.isEmpty()) {
            return null;
        }

        String downloadUrl = null;

        // Check for the desired quality in descending order
        for (var url : downloadUrls) {
            if (url.get("quality").text().equals("320kbps")) {
                downloadUrl = url.get("url").text();
                break;
            }
        }
        if (downloadUrl == null) {
            for (var url : downloadUrls) {
                if (url.get("quality").text().equals("160kbps")) {
                    downloadUrl = url.get("url").text();
                    break;
                }
            }
        }
        if (downloadUrl == null) {
            for (var url : downloadUrls) {
                if (url.get("quality").text().equals("96kbps")) {
                    downloadUrl = url.get("url").text();
                    break;
                }
            }
        }
        if (downloadUrl == null && !downloadUrls.isEmpty()) {
            downloadUrl = downloadUrls.get(0).get("url").text();
        }

        return downloadUrl;
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
