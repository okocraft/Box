import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
    `maven-publish`
}

val libs = extensions.getByType(LibrariesForLibs::class)
val release = findProperty("box.release")?.toString()?.toBoolean()

java {
    if (release != null) { // If null, there is no need to generate Javadoc or Sources.
        withJavadocJar()
        withSourcesJar()
    }
}

tasks {
    javadoc {
        val opts = options as StandardJavadocDocletOptions

        opts.encoding = Charsets.UTF_8.name()
        opts.addStringOption("Xdoclint:none", "-quiet")
        opts.links(
            "https://jd.papermc.io/paper/${libs.versions.paper.javadoc.get()}/",
            "https://jd.advntr.dev/api/${libs.versions.adventure.get()}/",
            "https://javadoc.io/doc/org.jetbrains/annotations/${libs.versions.annotations.get()}/",
            "https://siroshun09.github.io/ConfigAPI/${libs.versions.configapi.get()}",
            "https://siroshun09.github.io/Event4J/${libs.versions.event4j.get()}/",
            "https://siroshun09.github.io/TranslationLoader/"
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set(project.name)
                description.set("A Paper plugin to provide virtual containers that can store 2.1 billion items per item.")
                url.set("https://github.com/okocraft/Box")

                licenses {
                    license {
                        name.set("GNU General Public License, Version 3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/okocraft/Box.git")
                    developerConnection.set("scm:git:git@github.com:okocraft/Box.git")
                    url.set("https://github.com/okocraft/Box")
                }
            }
        }
    }

    repositories {
        maven {
            val stagingDir = rootDir.resolve("staging")

            url = if (release == true) { // Normally, null is never entered here.
                uri(stagingDir.resolve("maven"))
            } else {
                uri(stagingDir.resolve("maven-snapshot"))
            }
        }
    }
}
