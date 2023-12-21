plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(projects.boxApi)
}

tasks.javadoc {
    include("net/okocraft/box/feature/category/api/**")
}

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
