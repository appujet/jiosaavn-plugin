plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.lavalink)
    id("com.github.johnrengelman.shadow")
    id("com.github.breadmoirai.github-release")
}

val pluginVersion = findProperty("version") as String?
val commitSha = System.getenv("GITHUB_SHA")?.take(7) ?: "unknown"
val preRelease = System.getenv("PRERELEASE") == "true"
val verName = if (preRelease) commitSha else pluginVersion!!

group = "com.github.appujet"
version = verName
val archivesBaseName = "jiosaavn-plugin"

lavalinkPlugin {
    name = "jiosaavn-plugin"
    path = "$group.plugin"
    version = verName
    apiVersion = libs.versions.lavalink.api
    serverVersion = libs.versions.lavalink.server
    configurePublishing = false
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
        archiveVersion.set(verName)
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
        dependsOn(shadowJar)
    }
}

tasks.githubRelease {
    dependsOn(tasks.shadowJar)
    mustRunAfter(tasks.shadowJar)
}

data class Version(val major: Int, val minor: Int, val patch: Int) {
    override fun toString() = "$major.$minor.$patch"
}

if (System.getenv("USERNAME") != null && System.getenv("PASSWORD") != null) {
    publishing {
        repositories {
            maven {
                url = if (preRelease) {
                    uri("https://maven.appujet.site/snapshots")
                } else {
                    uri("https://maven.appujet.site/releases")
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

        publications {
            create<MavenPublication>("jiosaavn-plugin") {
                artifactId = "jiosaavn-plugin"
                version = verName
                groupId = project.group.toString()
                
                artifact(tasks.shadowJar.get()) {
                    builtBy(tasks.shadowJar)
                }
                
   
                pom {
                    name.set("jiosaavn-plugin")
                    description.set("JioSaavn plugin for Lavalink")
                }
            }
        }
    }
    
    tasks.withType<PublishToMavenRepository> {
        dependsOn(tasks.shadowJar)
        mustRunAfter(tasks.shadowJar)
    }
    
    tasks.withType<PublishToMavenLocal> {
        dependsOn(tasks.shadowJar)
        mustRunAfter(tasks.shadowJar)
    }
}

githubRelease {
    token(System.getenv("GITHUB_TOKEN"))
    owner("appujet")
    repo("jiosaavn-plugin")
    targetCommitish(System.getenv("RELEASE_TARGET"))
    releaseAssets(tasks.shadowJar.get().outputs.files.toList())
    tagName("v$verName")
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
            |          repository: https://maven.appujet.site/snapshots
            |```
        """.trimMargin())
    } else {
        body(changelog())
    }
}