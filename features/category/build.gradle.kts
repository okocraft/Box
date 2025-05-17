plugins {
    id("box.common-conventions")
    alias(libs.plugins.aggregated.javadoc.collector)
}

dependencies {
    implementation(projects.boxApi)
}

tasks.javadoc {
    include("net/okocraft/box/feature/category/api/**")
}
