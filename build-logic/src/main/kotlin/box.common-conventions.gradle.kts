plugins {
    `java-library`
    id("box.base-conventions")
    id("box.paper-repository")
    id("box.publication")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

dependencies {
    compileOnly(libs.paper)
    testImplementation(libs.paper)
}
