import java.io.ByteArrayOutputStream

plugins {
    `java-library`

    id("box.dependencies")
    id("box.publication")
}

val toUpload = findProperty("box.upload")?.toString()?.toBoolean() ?: false

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    build {
        if (toUpload) {
            buildDir = rootProject.buildDir.resolve(project.name)
        }
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    jar {
        var version = project.version.toString()

        if (toUpload) { // for GitHub Actions
            version = "${project.version}-git-${getLatestCommitHash()}"
        }

        manifest {
            attributes(
                "Implementation-Version" to version
            )
        }
    }
}

fun getLatestCommitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short=7", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}
