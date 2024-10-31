plugins {
    `java-library`
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

repositories {
    mavenCentral()
}

dependencies {
    api(libs.configapi.format.yaml) {
        exclude("org.yaml", "snakeyaml")
    }
    api(libs.event4j)
    api(libs.messages)

    compileOnly(libs.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.annotations)
    testImplementation(libs.fastutil)
    testImplementation(libs.mockito)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly(libs.slf4j.simple)
    testRuntimeOnly(libs.snakeyaml)
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
