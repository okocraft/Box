plugins {
    alias(libs.plugins.aggregated.javadoc.collector)
    alias(libs.plugins.mavenPublication)
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxCategoryFeature)
}

tasks.javadoc {
    include("net/okocraft/box/feature/gui/api/**")
}
