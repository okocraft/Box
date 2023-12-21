plugins {
    id("box.aggregate-javadocs")
}

tasks.aggregateJavadoc {
    val release = findProperty("box.release")?.toString()?.toBoolean()

    if (release != null) {
        val stagingDir = rootDir.resolve("staging")

        setDestinationDir(
            if (release == true) {
                stagingDir.resolve("release")
            } else {
                stagingDir.resolve("snapshot")
            }
        )
    }
}
