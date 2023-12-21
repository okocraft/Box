plugins {
    id("box.common-conventions")
}

tasks.javadoc {
    include("net/okocraft/box/api/**")
}

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
