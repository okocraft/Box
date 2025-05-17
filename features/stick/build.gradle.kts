plugins {
    alias(libs.plugins.aggregated.javadoc.collector)
    alias(libs.plugins.mavenPublication)
}

repositories {
    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://maven.playpro.com/")
    }
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxGuiFeature)
    compileOnly(libs.lwc)
    compileOnly(libs.bolt.bukkit)
    compileOnly(libs.bolt.common)
    compileOnly(libs.coreprotect)
    compileOnly(libs.worldguard) {
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
        exclude("it.unimi.dsi", "fastutil")
    }
}

tasks.javadoc {
    include("net/okocraft/box/feature/stick/StickFeature.java")
    include("net/okocraft/box/feature/stick/package-info.java")
    include("net/okocraft/box/feature/stick/event/stock/**")
    include("net/okocraft/box/feature/stick/item/**")
}
