plugins {
    alias(libs.plugins.mavenPublication)
}

dependencies {
    implementation(projects.boxApi)
    implementation(projects.boxItemProvider)
    implementation(projects.boxStorageApi)
    testImplementation(projects.boxTestSharedClasses)
}
