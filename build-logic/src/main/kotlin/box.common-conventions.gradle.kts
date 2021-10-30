plugins {
    `java-library`
    `maven-publish`
    id("box.dependencies")
    id("box.publication")
}

group = "net.okocraft.box"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(16)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}