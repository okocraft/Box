plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(projects.boxVersionCommon)
    compileOnly(projects.boxAnnotationProcessor)
    annotationProcessor(projects.boxAnnotationProcessor)
}
