plugins {
    id("box.common-conventions")
}

dependencies {
    api(projects.boxApi)
    api(projects.boxStorageApi)
    api(libs.junit.jupiter)
    api(libs.mockito)
    implementation(projects.boxCore)
}
