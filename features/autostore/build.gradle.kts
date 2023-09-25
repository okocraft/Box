plugins {
    id("box.common-conventions")
}

repositories {
    maven {
        url = uri("https://maven.playpro.com/")
    }
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxGuiFeature)
    compileOnly(libs.coreprotect)
}
