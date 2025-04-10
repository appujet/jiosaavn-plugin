rootProject.name = "jiosaavn-plugin-base"

include("main")
include("plugin")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            common()
            main()
            plugins()
        }
    }
}

fun VersionCatalogBuilder.common() {
    version("lavalink-api", "4.0.3")
    version("lavalink-server", "4.0.3")
}

fun VersionCatalogBuilder.main() {
    version("slf4j-version", "2.0.9")

    library("lavaplayer", "dev.arbjerg", "lavaplayer").version("2.2.1")
    library("logger", "org.slf4j", "slf4j-api").versionRef("slf4j-version")
    library("logger-impl", "org.slf4j", "slf4j-simple").versionRef("slf4j-version")
    library("commonsIo", "commons-io", "commons-io").version("2.7")
}

fun VersionCatalogBuilder.plugins() {
    plugin("lavalink", "dev.arbjerg.lavalink.gradle-plugin").version("1.0.15")
}