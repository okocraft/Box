repositories {
    maven {
        url = uri(rootDir.resolve("libs"))
        mavenContent {
            includeGroup(libs.kansokusha.api.get().module.group)
        }
    }
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(libs.kansokusha.api)

    testImplementation(projects.boxApi)
    testImplementation(libs.kansokusha.api)
}
