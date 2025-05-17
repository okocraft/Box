plugins {
    alias(libs.plugins.jcommon)
    id("box.aggregate-javadocs")
    id("box.properties")
}

tasks {
    aggregateJavadoc {
        if (boxBuildProperties.isPublishing) {
            val dirName = if (boxBuildProperties.isReleaseVersion) "release" else "snapshot"
            setDestinationDir(rootDir.resolve("staging").resolve(dirName))
        }
    }
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
        api(libs.paper)
        implementation(libs.configapi.serialization.record)

        compileOnly(libs.annotations)

        testImplementation(platform(libs.junit.bom))
        testImplementation(libs.junit.jupiter)
        testImplementation(libs.annotations)
        testImplementation(libs.fastutil)
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
}
