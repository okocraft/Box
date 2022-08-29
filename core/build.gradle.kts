plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(libs.translationloader)
    compileOnly(project(":box-api"))
    compileOnly(project(":box-storage-api"))
}
