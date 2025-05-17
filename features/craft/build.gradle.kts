plugins {
    id("box.common-conventions")
    alias(libs.plugins.aggregated.javadoc.collector)
}

dependencies {
    implementation(projects.boxApi)
    implementation(projects.boxGuiFeature)
}

tasks.javadoc {
    include("net/okocraft/box/feature/craft/package-info.java")
    include("net/okocraft/box/feature/craft/RecipeRegistry.java")
    include("net/okocraft/box/feature/craft/event/**")
    include("net/okocraft/box/feature/craft/model/**")
}
