dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxStorageApi)
    implementation(libs.configapi.serialization.record)

    testImplementation(projects.boxApi)
    testImplementation(projects.boxStorageApi)
    testImplementation(projects.boxTestSharedClasses)
}
