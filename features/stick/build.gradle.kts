plugins {
    id("box.common-conventions")
}

repositories {
    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxGuiFeature)
    compileOnly(libs.lwc)
    compileOnly(libs.bolt.bukkit)
    compileOnly(libs.bolt.common)
}
