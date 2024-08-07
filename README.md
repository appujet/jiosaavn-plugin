# JioSaavn Plugin For Lavalink

- This is a plugin for [Lavalink](https://github.com/lavalink-devs/Lavalink)
- This plugin allows you to play songs from JioSaavn in your discord server.
- This plugin uses the [JioSaavn API](https://github.com/sumitkolhe/jiosaavn-api) to fetch songs.
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
  port: 2333 # The port that the server listens on
  address: 0.0.0.0
  http2:
    enabled: false # Whether to enable HTTP/2 support
plugins:
  jiosaavn:
    apiURL: "https://saavn.dev/api" # JioSaavn API URL
  youtube:
    enabled: true # Whether this source can be used.
    allowSearch: true # Whether "ytsearch:" and "ytmsearch:" can be used.
    allowDirectVideoIds: true # Whether just video IDs can match. If false, only complete URLs will be loaded.
    allowDirectPlaylistIds: true # Whether just playlist IDs can match. If false, only complete URLs will be loaded.
    # The clients to use for track loading. See below for a list of valid clients.
    # Clients are queried in the order they are given (so the first client is queried first and so on...)
    clients:
      - MUSIC
      - WEB
      - ANDROID_TESTSUITE
      - ANDROID_MUSIC
      - TVHTML5EMBEDDED
      - ANDROID_LITE
      - MEDIA_CONNECT
      - IOS
lavalink:
  plugins:
    - dependency: "com.github.appujet:jiosaavn-plugin:VERSION"
      repository: "https://jitpack.io"
    - dependency: "dev.lavalink.youtube:youtube-plugin:1.5.2"
      snapshot: false # set to true if you want to use snapshot builds.
  pluginsDir: './plugins'
  server:
    password: "youshallnotpass"
    sources:
      # The default Youtube source is now deprecated and won't receive further updates. Please use https://github.com/lavalink-devs/youtube-source#plugin instead.
      youtube: false
      bandcamp: true
      soundcloud: true
      twitch: true
      vimeo: true
      mixer: true
      nico: true
      http: true # warning: keeping HTTP enabled without a proxy configured could expose your server's IP address.
      local: false
    filters: # All filters are enabled by default
      volume: true
      equalizer: true
      karaoke: true
      timescale: true
      tremolo: true
      vibrato: true
      distortion: true
      rotation: true
      channelMix: true
      lowPass: true
    bufferDurationMs: 400 # The duration of the NAS buffer. Higher values fare better against longer GC pauses. Duration <= 0 to disable JDA-NAS. Minimum of 40ms, lower values may introduce pauses.
    frameBufferDurationMs: 5000 # How many milliseconds of audio to keep buffered
    opusEncodingQuality: 10 # Opus encoder quality. Valid values range from 0 to 10, where 10 is best quality but is the most expensive on the CPU.
    resamplingQuality: MEDIUM # Quality of resampling operations. Valid values are LOW, MEDIUM and HIGH, where HIGH uses the most CPU.
    trackStuckThresholdMs: 10000 # The threshold for how long a track can be stuck. A track is stuck if does not return any audio data.
    useSeekGhosting: true # Seek ghosting is the effect where whilst a seek is in progress, the audio buffer is read from until empty, or until seek is ready.
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
    #youtubeConfig: # Required for avoiding all age restrictions by YouTube, some restricted videos still can be played without.
      #email: "your account mail" # Email of Google account
      #password: "your account password" # Password of Google account
    #httpConfig: # Useful for blocking bad-actors from ip-grabbing your music node and attacking it, this way only the http proxy will be attacked
      #proxyHost: "localhost" # Hostname of the proxy, (ip or domain)
      #proxyPort: 3128 # Proxy port, 3128 is the default for squidProxy
      #proxyUser: "" # Optional user for basic authentication fields, leave blank if you don't use basic auth
      #proxyPassword: "" # Password for basic authentication

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
    path: ./logs/

  level:
    # Set this to DEBUG to enable more detailed logging. Please note that this will likely spam your console.
    root: INFO
    # Set this to DEBUG to enable more detailed logging from Lavalink.
    lavalink: INFO

  request:
    enabled: true
    includeClientInfo: true
    includeHeaders: false
    includeQueryString: true
    includePayload: true
    maxPayloadLength: 10000

  logback:
    rollingpolicy:
      max-file-size: 1GB
      max-history: 30
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

- You can host the api locally using [this guide](https://github.com/sumitkolhe/jiosaavn-api)

- You can easily deploy your own instance of the API by clicking the button below:

[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=https://github.com/sumitkolhe/jiosaavn-api)

> [!TIP]
> To ensure the API provides results in the intended language, configure the [Serverless Function Region](https://vercel.com/docs/concepts/functions/serverless-functions/regions) in Vercel to `Mumbai, India (South) - > bom1`.
