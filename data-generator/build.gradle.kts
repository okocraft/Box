plugins {
    `java-library`
    alias(libs.plugins.bundler)
    alias(libs.plugins.run.server)
}

group = "net.okocraft.box.datagenerator"
version = "1.0"

val previousMinecraftVersion = "26.1.1"
val minecraftVersion = "26.1.2"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.paper)

    implementation(projects.boxCategoryFeature)
    implementation(projects.boxItemProvider)
}

bundler {
    replacePluginVersionForBukkit(version, minecraftVersion)
}

tasks {
    jar {
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }

    shadowJar {
        mergeServiceFiles()
    }

    runServer {
        minecraftVersion(minecraftVersion)
        systemProperty("com.mojang.eula.agree", "true")
        systemProperty("paper.disablePluginRemapping", "true")
        systemProperty(
            "net.okocraft.box.datagenerator.output.dir",
            layout.buildDirectory.dir("resources/generated-data").get().asFile.toPath().toAbsolutePath().toString()
        )
        systemProperty("net.okocraft.box.datagenerator.previous-version", previousMinecraftVersion)
    }
}
