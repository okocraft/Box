plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(libs.translationloader)
    compileOnly(projects.boxApi)
    compileOnly(projects.boxStorageApi)

    testImplementation(projects.boxApi)
    testImplementation(projects.boxStorageApi)
}
