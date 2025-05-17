plugins {
    alias(libs.plugins.aggregated.javadoc.collector)
    id("box.common-conventions")
}

tasks.javadoc {
    include("net/okocraft/box/api/**")
}

dependencies {
    testImplementation(projects.boxTestSharedClasses)
}
