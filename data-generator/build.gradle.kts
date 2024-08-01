plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.server)
}

group = "net.okocraft.box.datagenerator"
version = "1.0"

val previousMinecraftVersion = "1.20.6"
val minecraftVersion = "1.21"
val javaVersion = JavaVersion.VERSION_21

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")

    implementation(projects.boxCategoryFeature)
    rootProject.childProjects.values
        .filter { project -> project.name.startsWith("box-version-paper") }
        .forEach { project -> implementation(project) }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(javaVersion.ordinal + 1)
    }

    processResources {
        filesMatching(listOf("plugin.yml")) {
            expand("projectVersion" to version)
        }
    }

    jar {
        archiveFileName = "BoxDataGenerator-$version-original.jar"
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }

    shadowJar {
        archiveFileName = "BoxDataGenerator-$version.jar"
        mergeServiceFiles()
    }

    runServer {
        minecraftVersion(minecraftVersion)
        systemProperty("com.mojang.eula.agree", "true")
        systemProperty("paper.disablePluginRemapping", "true")
        systemProperty("net.okocraft.box.datagenerator.output.dir", layout.buildDirectory.dir("resources/generated-data").get().asFile.toPath().toAbsolutePath().toString())
        systemProperty("net.okocraft.box.datagenerator.previous-version", previousMinecraftVersion)
    }
}
