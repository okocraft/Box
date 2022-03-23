plugins {
    id("box.common-conventions")
}

dependencies {
    compileOnly(project(":box-api"))
    compileOnly(project(":box-gui-feature"))
}
