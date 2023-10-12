plugins {
    `java-library`
    `maven-publish`
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    api(libs.configapi.yaml)
    api(libs.event4j)

    compileOnly(libs.paper)
    compileOnly(libs.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.paper)
    testImplementation(libs.annotations)
    testImplementation(libs.fastutil)
}
