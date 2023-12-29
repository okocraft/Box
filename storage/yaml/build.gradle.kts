plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(projects.boxApi)
    implementation(projects.boxStorageApi)
    testImplementation(projects.boxTestSharedClasses)
}
