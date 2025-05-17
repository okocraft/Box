plugins {
    id("box.common-conventions")
    alias(libs.plugins.aggregated.javadoc.collector)
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxCategoryFeature)
}

tasks.javadoc {
    include("net/okocraft/box/feature/gui/api/**")
}
