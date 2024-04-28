plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.lavalink)
}

val pluginVersion = Version(0, 1, 0)

group = "com.github.appujet"
version = "$pluginVersion"
val archivesBaseName = "jiosaavn-plugin"
val preRelease = System.getenv("PRERELEASE") == "true"
val verName = "${if (preRelease) "PRE_" else ""}$pluginVersion${if(preRelease) "_${System.getenv("GITHUB_RUN_NUMBER")}" else ""}"

lavalinkPlugin {
    name = "jiosaavn-plugin"
    path = "$group.plugin"
    version = verName
    // Assuming libs and versions are defined elsewhere
    apiVersion = libs.versions.lavalink.api
    serverVersion = libs.versions.lavalink.server
}

dependencies {
    implementation(projects.main)
}

tasks {
    jar {
        archiveBaseName.set(archivesBaseName)
    }
}

data class Version(val major: Int, val minor: Int, val patch: Int) {
    override fun toString() = "$major.$minor.$patch"
}

publishing {
    publications {
        create<MavenPublication>("jitpack") {
            from(components["java"])
        }
    }
    repositories {
        maven(url = "https://jitpack.io")
    }
}
