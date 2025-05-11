plugins {
    id("box.aggregate-javadocs")
    id("box.properties")
}

tasks {
    register<Delete>("clean") {
        group = "build"
        layout.buildDirectory.get().asFile.deleteRecursively()
    }

    aggregateJavadoc {
        if (boxBuildProperties.isPublishing) {
            val dirName = if (boxBuildProperties.isReleaseVersion) "release" else "snapshot"
            setDestinationDir(rootDir.resolve("staging").resolve(dirName))
        }
    }
}
