plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(libs.translationloader)
    implementation(libs.configapi.format.yaml)
    compileOnly(projects.boxApi)
    compileOnly(projects.boxStorageApi)

    testImplementation(projects.boxApi)
    testImplementation(projects.boxStorageApi)
}
