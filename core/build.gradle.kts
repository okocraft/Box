plugins {
    id("box.common-conventions")
}

dependencies {
    implementation("com.github.siroshun09.translationloader:translationloader:2.0.0")
    compileOnly(project(":api"))
}
