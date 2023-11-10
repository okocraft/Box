plugins {
    id("box.common-conventions")
    alias(libs.plugins.shadow)
}

dependencies {
    rootProject.childProjects.values
        .filterNot { project -> project.name == "box-bundle" }
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
        filesMatching(listOf("paper-plugin.yml", "plugin.yml", "en.yml", "ja_JP.yml")) {
            expand("projectVersion" to project.version)
        }
    }

    shadowJar {
        minimize()
        relocate("com.github.siroshun09", "net.okocraft.box.lib")
        relocate("com.zaxxer", "net.okocraft.box.lib")
    }
}

fun getArtifactFilepath() : File {
    return rootProject.layout.buildDirectory.dir("libs").get().file("Box-${project.version}.jar").asFile
}
