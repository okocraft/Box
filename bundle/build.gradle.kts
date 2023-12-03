plugins {
    id("box.common-conventions")
    alias(libs.plugins.shadow)
}

dependencies {
    rootProject.childProjects.values
        .filterNot { project -> project.name == "box-bundle" }
        .forEach { project -> implementation(project) }

    implementation(libs.configapi.format.yaml)
    // TODO: Remove ConfigAPI v4 and TranslationLoader
    implementation(libs.configapi.yaml)
    implementation(libs.translationloader)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching(listOf("paper-plugin.yml", "plugin.yml", "en.yml", "ja_JP.yml")) {
            expand("projectVersion" to project.version)
        }
    }

    shadowJar {
        minimize()
        archiveFileName.set("Box-${project.version}.jar")
        relocate("com.github.siroshun09", "net.okocraft.box.lib")
        relocate("com.zaxxer", "net.okocraft.box.lib")
    }
}
