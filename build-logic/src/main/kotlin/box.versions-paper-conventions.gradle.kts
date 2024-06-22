plugins {
    `java-library`
    id("box.base-conventions")
    id("box.paper-repository")
}

dependencies {
    api(project(":box-version-common"))
    compileOnly(project(":box-annotation-processor"))
    annotationProcessor(project(":box-annotation-processor"))
}

afterEvaluate {
    dependencies.compileOnly("io.papermc.paper:paper-api:${project.extra["paper.version"]}-R0.1-SNAPSHOT")
}
