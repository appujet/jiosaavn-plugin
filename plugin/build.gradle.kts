plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.lavalink)
    id("com.github.johnrengelman.shadow")
    id("com.github.breadmoirai.github-release")
}

val pluginVersion = Version(0, 1, 1)

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

val impl = project.configurations.implementation.get()
impl.isCanBeResolved = true

tasks {
    jar {
        archiveBaseName.set(archivesBaseName)
    }
    shadowJar {
        archiveBaseName.set(archivesBaseName)
        archiveClassifier.set("")

        configurations = listOf(impl)
    }
    build {
        dependsOn(processResources)
        dependsOn(compileJava)
        dependsOn(shadowJar)
    }
    publish {
        dependsOn(publishToMavenLocal)
    }
}

tasks.githubRelease {
    dependsOn(tasks.jar)
    dependsOn(tasks.shadowJar)
    mustRunAfter(tasks.shadowJar)
}

data class Version(val major: Int, val minor: Int, val patch: Int) {
    override fun toString() = "$major.$minor.$patch"
}


publishing {
    repositories {
        maven {
            url = if (preRelease) {
                uri("https://maven.lavalink.dev/snapshots")
            } else {
                uri("https://maven.lavalink.dev/releases")
            }
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

githubRelease {
    token(System.getenv("GITHUB_TOKEN"))
    owner("appujet")
    repo("jiosaavn-plugin")
    targetCommitish(System.getenv("RELEASE_TARGET"))
    releaseAssets(tasks.shadowJar.get().outputs.files.toList())
    tagName(verName)
    releaseName(verName)
    overwrite(false)
    prerelease(preRelease)

    if (preRelease) {
        body("""Here is a pre-release version of the plugin. Please test it and report any issues you find.
            |Example:
            |```yml
            |lavalink:
            |    plugins:
            |        - dependency: "com.github.appujet:jiosaavn-plugin:$verName"
            |          repository: https://jitpack.io
            |```
        """.trimMargin())
    } else {
        body(changelog())
    }
}