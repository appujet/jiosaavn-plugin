plugins {
    `java-library`
//    `maven-publish`
}

project.group = "com.github.appujet"
project.version = "0.1.2"
val archivesBaseName = "jiosaavn"

dependencies {
    compileOnly(libs.lavaplayer)
    compileOnly("org.jetbrains:annotations:24.1.0")
    implementation(libs.logger)
    implementation(libs.commonsIo)

    testImplementation(libs.lavaplayer)
    testImplementation(libs.logger.impl)
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val jar: Jar by tasks
val build: Task by tasks
val clean: Task by tasks
//val publish: Task by tasks

val sourcesJar = task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allJava)
}

build.apply {
//    dependsOn(clean)
    dependsOn(jar)
    dependsOn(sourcesJar)

    jar.mustRunAfter(clean)
    sourcesJar.mustRunAfter(jar)
}