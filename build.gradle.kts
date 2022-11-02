plugins {
    // I wrote it as in the example, and it seems to work normally, but it shows an error on idea.
    alias(libs.plugins.aggregate.javadoc)
}

tasks {
    aggregateJavadoc {
        sequenceOf(
            "api/**",
            "feature/autostore/event/AutoStoreSettingChangeEvent.java",
            "feature/autostore/model/**",
            "feature/category/CategoryHolder.java",
            "feature/category/model/Category.java",
            "feature/gui/api/**",
            "feature/stick/StickFeature.java",
            "feature/stick/item/BoxStickItem.java"
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
