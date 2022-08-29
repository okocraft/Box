plugins {
    id("io.freefair.aggregate-javadoc") version "6.5.0.3"
}

tasks {
    aggregateJavadoc {
        sequenceOf(
            "api/**",
            "feature/autostore/event/AutoStoreSettingChangeEvent.java",
            "feature/autostore/model/**",
            "feature/category/CategoryHolder.java",
            "feature/category/model/Category.java",
            "feature/gui/api/**"
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
