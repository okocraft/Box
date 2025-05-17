plugins {
    alias(libs.plugins.mavenPublication)
}

dependencies {
    compileOnly(projects.boxApi)
    testImplementation(projects.boxTestSharedClasses)
}
