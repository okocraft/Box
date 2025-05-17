plugins {
    alias(libs.plugins.aggregated.javadoc.collector)
    alias(libs.plugins.mavenPublication)
}

tasks.javadoc {
    include("net/okocraft/box/api/**")
}

dependencies {
    testImplementation(projects.boxTestSharedClasses)
}
