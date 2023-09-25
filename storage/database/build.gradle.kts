plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(projects.boxApi)
    implementation(projects.boxStorageApi)

    implementation(libs.hikaricp) {
        exclude("org.slf4j")
    }
}
