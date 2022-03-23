import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("box.common-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    rootProject.childProjects.values
        .filterNot { project -> project.name == "box-bundle" }
        .forEach { project -> implementation(project); println(project) }
}

tasks.named<Copy>("processResources") {
    filesMatching(listOf("plugin.yml", "en.yml", "ja_JP.yml")) {
        expand("projectVersion" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    minimize()
    archiveFileName.set("Box-${project.version}.jar")
    relocate("com.github.siroshun09", "net.okocraft.box.lib")
}
