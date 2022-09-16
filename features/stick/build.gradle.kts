plugins {
    id("box.common-conventions")
}

repositories {
    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(project(":box-api"))
    compileOnly(libs.lwc)
}
