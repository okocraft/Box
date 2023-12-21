plugins {
    id("box.common-conventions")
}

repositories {
    maven {
        url = uri("https://maven.playpro.com/")
    }
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxGuiFeature)
    compileOnly(libs.coreprotect)
}

tasks.javadoc {
    include("net/okocraft/box/feature/autostore/event/**")
    include("net/okocraft/box/feature/autostore/model/**")
}

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
