package com.github.appujet.jiosaavn.source;

import com.github.appujet.jiosaavn.ExtendedAudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.List;

public class JioSaavnAudioPlaylist extends ExtendedAudioPlaylist {
    public JioSaavnAudioPlaylist(String name, List<AudioTrack> tracks, Type type, String url, String artworkURL, String author, Integer totalTracks) {
        super(name, tracks, type, url, artworkURL, author, totalTracks);
    }
}