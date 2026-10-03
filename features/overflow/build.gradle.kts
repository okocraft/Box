plugins {
    alias(libs.plugins.mavenPublication)
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxStorageApi)

    testImplementation(projects.boxCore)
    testImplementation(projects.boxTestSharedClasses)
}
