plugins {
    id("box.common-conventions")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":gui"))
}
