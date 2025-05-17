plugins {
    alias(libs.plugins.aggregated.javadoc.collector)
    alias(libs.plugins.mavenPublication)
}

dependencies {
    implementation(projects.boxApi)
}

tasks.javadoc {
    include("net/okocraft/box/feature/category/api/**")
}
