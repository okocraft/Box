plugins {
    alias(libs.plugins.bundler)
    alias(libs.plugins.run.server)
}

dependencies {
    rootProject.childProjects.values
        .filterNot { project -> project.name == "box-annotation-processor" }
        .filterNot { project -> project.name == "box-data-generator" }
        .filterNot { project -> project.name == "box-bundle" }
        .filterNot { project -> project.name == "box-test-shared-classes" }
        .forEach { project -> implementation(project) }
}

bundler {
    copyToRootBuildDirectory("Box-${project.version}")
    replacePluginVersionForPaper(project.version)
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }

    runServer {
        minecraftVersion("1.21.10")
        systemProperty("com.mojang.eula.agree", "true")
        systemProperty("paper.disablePluginRemapping", "true")
    }
}
