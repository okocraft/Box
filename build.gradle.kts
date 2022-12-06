plugins {
    // I wrote it as in the example, and it seems to work normally, but it shows an error on idea.
    alias(libs.plugins.aggregate.javadoc)
}

tasks {
    aggregateJavadoc {
        sequenceOf(
            "api/**",
            "feature/autostore/event/**",
            "feature/autostore/model/**",
            "feature/category/api/**",
            "feature/gui/api/**",
            "feature/stick/StickFeature.java",
            "feature/stick/package-info.java",
            "feature/stick/item/BoxStickItem.java",
            "feature/stick/item/package-info.java",
        ).forEach { include("net/okocraft/box/$it") }

        (options as StandardJavadocDocletOptions).docTitle("Box-$version")

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
}
