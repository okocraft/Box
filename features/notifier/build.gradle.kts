plugins {
    id("box.common-conventions")
}

dependencies {
    compileOnly(projects.boxApi)
    testImplementation(projects.boxTestSharedClasses)
}
