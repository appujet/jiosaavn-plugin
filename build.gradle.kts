plugins {
    java
    alias(libs.plugins.lavalink) apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("com.github.breadmoirai.github-release") version "2.4.1" apply false
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

allprojects {
    group = "com.github.appujet"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")
        maven("https://maven.appujet.site/releases")
        maven("https://maven.appujet.site/snapshots")
        maven("https://jitpack.io")
    }

    tasks.withType<Wrapper> {
        gradleVersion = "8.4"
        distributionType = Wrapper.DistributionType.BIN
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
dependencies {
    implementation(kotlin("script-runtime"))
}