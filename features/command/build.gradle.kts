plugins {
    alias(libs.plugins.aggregated.javadoc.collector)
    alias(libs.plugins.mavenPublication)
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxStorageApi)

    implementation(libs.codec4j.io.gson) {
        exclude("com.google.code.gson", "gson")
    }
}

tasks.javadoc {
    include("net/okocraft/box/feature/command/event/stock/**")
}
