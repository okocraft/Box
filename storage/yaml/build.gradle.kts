plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(projects.boxApi)
    implementation(projects.boxStorageApi)
    implementation(libs.configapi.format.yaml)
}
