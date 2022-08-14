plugins {
    id("box.common-conventions")
}

dependencies {
    implementation(project(":box-api"))
    implementation(project(":box-storage-api"))

    implementation("com.zaxxer:HikariCP:5.0.1")
}
