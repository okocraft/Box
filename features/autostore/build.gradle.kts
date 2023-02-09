plugins {
    id("box.common-conventions")
}

repositories {
    maven {
        url = uri("https://maven.playpro.com/")
    }
}

dependencies {
    compileOnly(project(":box-api"))
    compileOnly(project(":box-gui-feature"))
    compileOnly(libs.coreprotect)
}
