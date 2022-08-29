repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(files(libs::class.java.protectionDomain.codeSource.location))
}

plugins {
    `kotlin-dsl` apply true
}
