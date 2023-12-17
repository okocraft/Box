plugins {
    `java-library`
    `maven-publish`
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

repositories {
    mavenCentral()

    // for snapshots
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
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

    compileOnly(libs.paper)
    compileOnly(libs.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.paper)
    testImplementation(libs.annotations)
    testImplementation(libs.fastutil)
    testRuntimeOnly(libs.snakeyaml)
}
