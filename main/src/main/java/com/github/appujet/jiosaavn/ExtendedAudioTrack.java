package com.github.appujet.jiosaavn;


import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class ExtendedAudioTrack extends DelegatedAudioTrack {
    protected static final Logger log = LoggerFactory.getLogger(ExtendedAudioTrack.class);

    private final ExtendedAudioSourceManager manager;

    public ExtendedAudioTrack(AudioTrackInfo trackInfo, ExtendedAudioSourceManager manager) {
        super(trackInfo);
        this.manager = manager;
    }

    protected HttpInterface getHttpInterface() {
        return this.manager.getHttpInterface();
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        try (HttpInterface httpInterface = getHttpInterface()) {
            loadStream(executor, httpInterface);
        }
    }

    protected void loadStream(LocalAudioTrackExecutor localExecutor, HttpInterface httpInterface) throws Exception {
        final String trackUrl = getPlaybackUrl();
        try (final var stream = this
                .wrapStream(new PersistentHttpStream(httpInterface, new URI(trackUrl), this.getTrackDuration()))) {
            processDelegate(createAudioTrack(this.trackInfo, stream), localExecutor);
        } catch (Exception e) {
            log.error("Failed to load track from URL: {}", trackUrl, e);
            throw e;
        }
    }
    
    protected SeekableInputStream wrapStream(SeekableInputStream stream) {
        return stream;
    }
    
    protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
        return new MpegAudioTrack(trackInfo, stream);
    }

    protected long getTrackDuration() {
        return this.trackInfo.length;
    }

    public String getPlaybackUrl() {
        return this.trackInfo.identifier;
    }

    @Override
    public ExtendedAudioSourceManager getSourceManager() {
        return manager;
    }
}
