package com.github.appujet.plugin;


import com.github.appujet.jiosaavn.source.JioSaavnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JiosaavanPlugin implements AudioPlayerManagerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JiosaavanPlugin.class);

    private final JioSaavanConfig sourcesConfig;
    private JioSaavnAudioSourceManager jioSaavn;

    public JiosaavanPlugin(JioSaavanConfig sourcesConfig) {
        log.info("Loaded JioSaavn plugin...");
        this.sourcesConfig = sourcesConfig;
        if (this.sourcesConfig.getApiURL() != null) {
            this.jioSaavn = new JioSaavnAudioSourceManager(this.sourcesConfig.getApiURL());
        }
    }

    @NotNull
    @Override
    public AudioPlayerManager configure(@NotNull AudioPlayerManager manager) {
        if (this.sourcesConfig.getApiURL() != null) {
            log.info("Registering JioSaavn audio source manager...");
            manager.registerSourceManager(this.jioSaavn);
        } else {
            log.warn("JioSaavn audio source manager not registered, no API URL provided");
        }
        return manager;
    }
}
