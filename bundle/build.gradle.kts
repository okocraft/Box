plugins {
    alias(libs.plugins.bundler)
    alias(libs.plugins.run.server)
}

dependencies {
    rootProject.childProjects.values
        .filterNot { project -> project.name == "box-data-generator" }
        .filterNot { project -> project.name == "box-bundle" }
        .filterNot { project -> project.name == "box-test-shared-classes" }
        .forEach { project -> implementation(project) }
}

bundler {
    copyToRootBuildDirectory("Box-${project.version}")
    replacePluginVersionForPaper(project.version)
}

val mcVersion = libs.versions.paper.asProvider().get().replaceAfter(".build", "").removeSuffix(".build")

tasks {
    shadowJar {
        mergeServiceFiles()
    }

    runServer {
        minecraftVersion(mcVersion)
        systemProperty("com.mojang.eula.agree", "true")
        systemProperty("paper.disablePluginRemapping", "true")
    }
}
