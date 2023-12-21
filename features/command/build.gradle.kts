plugins {
    id("box.common-conventions")
}

dependencies {
    compileOnly(projects.boxApi)
}

tasks.javadoc {
    include("net/okocraft/box/feature/command/event/stock/**")
}

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
