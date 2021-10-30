plugins {
    id("box.feature-conventions")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":category"))
}
