plugins {
    id("box.common-conventions")
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxStorageApi)

    testImplementation(projects.boxApi)
    testImplementation(projects.boxStorageApi)
    testImplementation(projects.boxTestSharedClasses)
}
