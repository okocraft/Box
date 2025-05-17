plugins {
    alias(libs.plugins.mavenPublication)
}

dependencies {
    implementation(projects.boxApi)
    implementation(projects.boxStorageApi)
    testImplementation(projects.boxTestSharedClasses)
}
