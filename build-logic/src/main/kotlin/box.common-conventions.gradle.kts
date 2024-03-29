plugins {
    `java-library`
    id("box.dependencies")
    id("box.publication")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    jar {
        manifest {
            attributes(
                "Implementation-Version" to project.version.toString()
            )
        }
    }

    test {
        // See https://github.com/mockito/mockito/issues/3037
        jvmArgs("-XX:+EnableDynamicAgentLoading")

        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
