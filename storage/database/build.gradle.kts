plugins {
    alias(libs.plugins.mavenPublication)
}

dependencies {
    implementation(projects.boxApi)
    implementation(projects.boxItemProvider)
    implementation(projects.boxStorageApi)

    implementation(libs.configapi.format.binary)

    testImplementation(projects.boxTestSharedClasses)
    testRuntimeOnly(libs.sqlite.jdbc)
}
