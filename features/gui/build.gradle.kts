plugins {
    id("box.common-conventions")
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxCategoryFeature)
}

tasks.javadoc {
    include("net/okocraft/box/feature/gui/api/**")
}

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
