package com.github.appujet.plugin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "plugins.jiosaavn")
public class JioSaavanConfig {
    private String apiURL = null;
    public String getApiURL() {
        return apiURL;
    }

    public void setApiURL(String apiURL) {
        this.apiURL = apiURL;
    }
}