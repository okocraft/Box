plugins {
    id("box.common-conventions")
    // I wrote it as in the example, and it seems to work normally, but it shows an error on idea.
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
    }

    processResources {
        filesMatching(listOf("plugin.yml", "en.yml", "ja_JP.yml")) {
            expand("projectVersion" to project.version)
        }
    }

    shadowJar {
        minimize()
        archiveFileName.set("Box-${project.version}.jar")
        relocate("com.github.siroshun09", "net.okocraft.box.lib")
    }
}
