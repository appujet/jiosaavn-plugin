package com.github.appujet.jiosaavn;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import org.jetbrains.annotations.NotNull;


import java.util.List;

public class ExtendedAudioPlaylist extends BasicAudioPlaylist {

    @NotNull
    protected final Type type;
    protected final @NotNull String url;
    protected final @NotNull String artworkURL;
    protected final @NotNull String author;
    protected final @NotNull Integer totalTracks;

    public ExtendedAudioPlaylist(String name, List<AudioTrack> tracks, @NotNull Type type, @NotNull String url, @NotNull String artworkURL, @NotNull String author, @NotNull Integer totalTracks) {
        super(name, tracks, null, false);
        this.type = type;
        this.url = url;
        this.artworkURL = artworkURL;
        this.author = author;
        this.totalTracks = totalTracks;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public @NotNull String getUrl() {
        return this.url;
    }

    public @NotNull String getArtworkURL() {
        return this.artworkURL;
    }

    public @NotNull String getAuthor() {
        return this.author;
    }

    public @NotNull Integer getTotalTracks() {
        return this.totalTracks;
    }

    public enum Type {
        ALBUM("album"),
        PLAYLIST("playlist"),
        ARTIST("artist"),
        RECOMMENDATIONS("recommendations");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
