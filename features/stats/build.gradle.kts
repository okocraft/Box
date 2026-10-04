plugins {
    alias(libs.plugins.mavenPublication)
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxGuiFeature)
    compileOnly(projects.boxStorageApi)
    compileOnly(projects.boxStorageDatabase)
    testImplementation(projects.boxTestSharedClasses)
    testImplementation(projects.boxStorageDatabase)
    testRuntimeOnly(libs.sqlite.jdbc)
}
