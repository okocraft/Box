plugins {
    id("box.common-conventions")
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

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
