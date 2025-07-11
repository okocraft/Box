plugins {
    alias(libs.plugins.jcommon)
    alias(libs.plugins.aggregated.javadoc)
    alias(libs.plugins.mavenPublication)
}

val isReleaseVersion = !project.version.toString().endsWith("-SNAPSHOT")

aggregatedJavadoc {
    val dirName = if (isReleaseVersion) "release" else "snapshot"
    outputDir = rootDir.resolve("staging").resolve(dirName)
}

jcommon {
    javaVersion = JavaVersion.VERSION_21

    setupPaperRepository()

    commonDependencies {
        api(libs.configapi.format.yaml) {
            exclude("org.yaml", "snakeyaml")
        }
        api(libs.event4j)
        api(libs.messages)
        compileOnlyApi(libs.paper)
        implementation(libs.configapi.serialization.record)

        compileOnly(libs.annotations)

        testImplementation(platform(libs.junit.bom))
        testImplementation(libs.junit.jupiter)
        testImplementation(libs.annotations)
        testImplementation(libs.paper)

        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testRuntimeOnly(libs.slf4j.simple)
        testRuntimeOnly(libs.snakeyaml)
    }

    setupMockito(libs.mockito)

    jarTask {
        manifest {
            attributes(
                "Implementation-Version" to project.version.toString()
            )
        }
    }

    javadocTask {
        val opts = options as StandardJavadocDocletOptions

        opts.encoding = Charsets.UTF_8.name()
        opts.addStringOption("Xdoclint:none", "-quiet")
        opts.links(
            "https://jd.papermc.io/paper/${libs.versions.paper.javadoc.get()}/",
            "https://jd.advntr.dev/api/${libs.versions.adventure.get()}/",
            //"https://javadoc.io/doc/org.jetbrains/annotations/${libs.versions.annotations.get()}/",
            "https://siroshun09.github.io/ConfigAPI/${libs.versions.configapi.get()}",
            "https://siroshun09.github.io/Event4J/${libs.versions.event4j.get()}/"
        )
    }
}

mavenPublication {
    val dirName = if (isReleaseVersion) "maven" else "maven-snapshot"
    localRepository(rootProject.projectDir.resolve("staging").resolve(dirName))
    description("A Paper plugin to provide virtual containers that can store 2.1 billion items per item.")
    gplV3License()
    developer("Siroshun09")
    developer("lazy_gon")
    github("okocraft/Box")
}
