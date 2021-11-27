plugins {
    id("box.common-conventions")
}

dependencies {
    implementation("com.github.siroshun09.translationloader:translationloader:1.1.1")
    compileOnly(project(":api"))
}
