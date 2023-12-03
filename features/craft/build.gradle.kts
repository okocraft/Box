plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(projects.boxApi)
    implementation(projects.boxGuiFeature)
    implementation(libs.configapi.format.yaml)
}
