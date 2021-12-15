import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("box.common-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.1"
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":autostore"))
    implementation(project(":category"))
    implementation(project(":command"))
    implementation(project(":craft"))
    implementation(project(":gui"))
    implementation(project(":notifier"))
    implementation(project(":stick"))
}

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("projectVersion" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    minimize()
    archiveFileName.set("Box-${project.version}.jar")
    relocate("com.github.siroshun09", "net.okocraft.box.lib")
}
