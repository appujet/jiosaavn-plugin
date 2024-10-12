package com.github.appujet.plugin;

import com.github.appujet.jiosaavn.source.JioSaavnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JiosaavnPlugin implements AudioPlayerManagerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JiosaavnPlugin.class);

    private final JioSaavnConfig sourcesConfig;
    private final JioSaavnAudioSourceManager jioSaavn;

    public JiosaavnPlugin(JioSaavnConfig sourcesConfig) {
        log.info("Loaded JioSaavn plugin...");
        this.sourcesConfig = sourcesConfig;

        this.jioSaavn = new JioSaavnAudioSourceManager(
                this.sourcesConfig.getPlaylistTrackLimit(),
                this.sourcesConfig.getRecommendationsTrackLimit());

    }

    @NotNull
    @Override
    public AudioPlayerManager configure(@NotNull AudioPlayerManager manager) {
        log.info("Registering JioSaavn audio source manager...");
        manager.registerSourceManager(this.jioSaavn);
        return manager;
    }
}
