plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(project(":box-api"))
    implementation(project(":box-storage-api"))

    implementation(libs.hikaricp) {
        exclude("org.slf4j")
    }
}
