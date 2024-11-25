# JioSaavn Plugin For Lavalink

[![JitPack](https://jitpack.io/v/appujet/jiosaavn-plugin.svg)](https://jitpack.io/#appujet/jiosaavn-plugin)

- This is a plugin for [Lavalink](https://github.com/lavalink-devs/Lavalink)
- This plugin allows you to play songs from JioSaavn in your discord server.
- This plugin uses the [JioSaavn API](https://github.com/appujet/jiosaavn-plugin-api) to fetch songs.
- Special thanks to [topi314](https://github.com/topi314/LavaSrc) and [duncte123](https://github.com/duncte123) because most of the code for this plugin is based on [Lavasrc](https://github.com/topi314/LavaSrc) and [skybot-lavalink-plugin](https://github.com/DuncteBot/skybot-lavalink-plugin).

## Lavalink Usage
To install this plugin either download the latest release and place it into your plugins folder or add the following into your application.yml
Replace `VERSION` with the latest release version.

```yaml
lavalink:
  plugins:
    - dependency: "com.github.appujet:jiosaavn-plugin:VERSION"
      repository: "https://jitpack.io"
```

## Configuration

For all supported urls and queries see [here](#supported-urls-and-queries)

(YES `plugins` IS AT ROOT IN THE YAML)

```yaml
server: # REST and WS server
  port: 2333
  address: 0.0.0.0
lavalink:
# plugins would go here, but they are auto-loaded when developing
#  plugins:
#    - dependency: "com.github.appujet:jiosaavn-plugin:VERSION"
#      repository: "https://jitpack.io"
  server:
    password: "youshallnotpass"
    sources:
      youtube: true
      bandcamp: true
      soundcloud: true
      twitch: true
      vimeo: true
      http: true
      local: false
    bufferDurationMs: 400 # The duration of the NAS buffer. Higher values fare better against longer GC pauses
    frameBufferDurationMs: 5000 # How many milliseconds of audio to keep buffered
    youtubePlaylistLoadLimit: 6 # Number of pages at 100 each
    playerUpdateInterval: 5 # How frequently to send player updates to clients, in seconds
    youtubeSearchEnabled: true
    soundcloudSearchEnabled: true
    gc-warnings: true
    #ratelimit:
      #ipBlocks: ["1.0.0.0/8", "..."] # list of ip blocks
      #excludedIps: ["...", "..."] # ips which should be explicit excluded from usage by lavalink
      #strategy: "RotateOnBan" # RotateOnBan | LoadBalance | NanoSwitch | RotatingNanoSwitch
      #searchTriggersFail: true # Whether a search 429 should trigger marking the ip as failing
      #retryLimit: -1 # -1 = use default lavaplayer value | 0 = infinity | >0 = retry will happen this numbers times

plugins:
  jiosaavn:
    apiURL: "https://jiosaavn-plugin-api.vercel.app/api" # JioSaavn API URL
    playlistTrackLimit: 50 # The maximum number of tracks to return from given playlist (default 50 tracks)
    recommendationsTrackLimit: 10 # The maximum number of track to return from recommendations (default 10 tracks)
    
metrics:
  prometheus:
    enabled: false
    endpoint: /metrics

sentry:
  dsn: ""
  environment: ""
#  tags:
#    some_key: some_value
#    another_key: another_value

logging:
  file:
    max-history: 30
    max-size: 1GB
  path: ./logs/

  level:
    root: INFO
    lavalink: INFO
```

## Advantages of Using JioSaavn

- No region-based content blocking (unlike Deezer and Yandex).
- A better alternative for playing mirrored audio sources not dependent on YouTube.
- Similar content library size as Spotify and Apple Music.
- Superior to Deezer as it doesn't require any decryption key and provides slightly higher bitrate audio than Deezer's 128KBPS MP3.

## Supported URLs and Queries

### JioSaavn

- `jssearch:animals architects` - Search for a song on JioSaavn.
- `jsrec:identifier` - Get a song recommendation based on the identifier.

- <https://www.jiosaavn.com/song/apna-bana-le/ATIfejZ9bWw>
- <https://www.jiosaavn.com/album/bhediya/wSM2AOubajk>_
- <https://www.jiosaavn.com/artist/arijit-singh-songs/LlRWpHzy3Hk>_
- <https://www.jiosaavn.com/featured/jai-hanuman/8GIEhrr8clSO0eMLZZxqsA>__

## How to get API URL ?

- You can host the api locally using [this guide](https://github.com/appujet/jiosaavn-plugin-api)
