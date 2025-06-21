plugins {
    alias(libs.plugins.mavenPublication)
}

dependencies {
    implementation(projects.boxApi)
    implementation(libs.configapi.codec)
    implementation(libs.codec4j.io.gson) {
        exclude("com.google.code.gson", "gson")
    }
    implementation(libs.codec4j.io.gzip)
}
