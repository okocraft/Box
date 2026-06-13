dependencies {
    api(projects.boxApi)
    api(projects.boxItemProvider)
    api(projects.boxStorageApi)
    api(libs.junit.jupiter)
    api(libs.mockito)
    implementation(projects.boxCore)
}
