dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxItemProvider)
    compileOnly(projects.boxStorageApi)
    implementation(libs.configapi.serialization.record)

    testImplementation(projects.boxApi)
    testImplementation(projects.boxItemProvider)
    testImplementation(projects.boxStorageApi)
    testImplementation(projects.boxTestSharedClasses)
}
