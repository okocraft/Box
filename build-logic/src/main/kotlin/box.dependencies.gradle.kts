plugins {
    `java-library`
    `maven-publish`
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)
val enableSnapshotRepo = false // For updating Box when a newer Minecraft version is released.

repositories {
    mavenCentral()

    if (enableSnapshotRepo) {
        sequenceOf(
            "https://oss.sonatype.org/content/repositories/snapshots/",
            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        ).forEach {
            maven {
                url = uri(it)
            }
        }
    }

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    api(libs.configapi.format.yaml) {
        exclude("org.yaml", "snakeyaml")
    }
    api(libs.event4j)
    api(libs.messages)

    compileOnly(libs.paper)
    compileOnly(libs.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.annotations)
    testImplementation(libs.configapi.test.shared.classes)
    testImplementation(libs.fastutil)
    testImplementation(libs.mockito)
    testImplementation(libs.paper)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly(libs.slf4j.simple)
    testRuntimeOnly(libs.snakeyaml)
}
