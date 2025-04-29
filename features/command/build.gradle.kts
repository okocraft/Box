plugins {
    id("box.common-conventions")
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxStorageApi)

    implementation(libs.codec4j.io.gson) {
        exclude("com.google.code.gson", "gson")
    }
}

tasks.javadoc {
    include("net/okocraft/box/feature/command/event/stock/**")
}

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
