plugins {
    id("box.common-conventions")
}

dependencies {
    implementation("com.github.siroshun09.translationloader:translationloader:2.0.2")
    compileOnly(project(":box-api"))
    compileOnly(project(":box-storage-api"))
}
