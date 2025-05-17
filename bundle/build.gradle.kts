plugins {
    alias(libs.plugins.shadow)
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

tasks {
    build {
        dependsOn(shadowJar)
        doLast {
            val filepath = getArtifactFilepath()
            filepath.parentFile.mkdirs()
            shadowJar.get().archiveFile.get().asFile.copyTo(getArtifactFilepath(), true)
        }
    }

    clean {
        doLast {
            getArtifactFilepath().delete()
        }
    }

    processResources {
        filesMatching(listOf("paper-plugin.yml")) {
            expand("projectVersion" to project.version)
        }
    }

    shadowJar {
        mergeServiceFiles()
    }

    runServer {
        minecraftVersion("1.21.5")
        systemProperty("com.mojang.eula.agree", "true")
        systemProperty("paper.disablePluginRemapping", "true")
    }
}

fun getArtifactFilepath(): File {
    return rootProject.layout.buildDirectory.dir("libs").get().file("Box-${project.version}.jar").asFile
}
