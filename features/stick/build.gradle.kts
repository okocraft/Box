plugins {
    id("box.common-conventions")
}

repositories {
    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
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
    compileOnly(libs.worldguard)
}

tasks.javadoc {
    include("net/okocraft/box/feature/stick/StickFeature.java")
    include("net/okocraft/box/feature/stick/package-info.java")
    include("net/okocraft/box/feature/stick/event/stock/**")
    include("net/okocraft/box/feature/stick/item/**")
}

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
