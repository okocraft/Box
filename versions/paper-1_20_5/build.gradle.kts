plugins {
    id("box.common-conventions")
}

dependencies {
    api(projects.boxVersionCommon)
    compileOnly(projects.boxAnnotationProcessor)
    annotationProcessor(projects.boxAnnotationProcessor)
}
