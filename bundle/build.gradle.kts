plugins {
    id("box.common-conventions")
    alias(libs.plugins.shadow)
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
}

fun getArtifactFilepath() : File {
    return rootProject.layout.buildDirectory.dir("libs").get().file("Box-${project.version}.jar").asFile
}
