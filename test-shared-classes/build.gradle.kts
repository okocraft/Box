plugins {
    id("box.common-conventions")
}

dependencies {
    api(projects.boxApi)
    api(projects.boxStorageApi)
    api(libs.junit.jupiter)
    implementation(projects.boxCore)
}
