plugins {
    alias(libs.plugins.aggregate.javadoc)
    id("box.properties")
}

tasks {
    register<Delete>("clean") {
        group = "build"
        layout.buildDirectory.get().asFile.deleteRecursively()
    }

    aggregateJavadoc {
        sequenceOf(
            "api/**",
            "feature/autostore/event/**",
            "feature/autostore/model/**",
            "feature/category/api/**",
            "feature/command/event/stock/**",
            "feature/craft/package-info.java",
            "feature/craft/RecipeRegistry.java",
            "feature/craft/model/**",
            "feature/craft/event/**",
            "feature/gui/api/**",
            "feature/stick/StickFeature.java",
            "feature/stick/package-info.java",
            "feature/stick/event/stock/**",
            "feature/stick/item/BoxStickItem.java",
            "feature/stick/item/package-info.java",
        ).forEach { include("net/okocraft/box/$it") }

        (options as StandardJavadocDocletOptions).docTitle("Box $version").windowTitle("Box $version")

        if (boxBuildProperties.isPublishing) {
            val dirName = if (boxBuildProperties.isReleaseVersion) "release" else "snapshot"
            setDestinationDir(rootDir.resolve("staging").resolve(dirName))
        }
    }
}
