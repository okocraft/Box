plugins {
    `java-library`
    `maven-publish`
}

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
            "https://jd.papermc.io/paper/1.19/",
            "https://jd.adventure.kyori.net/api/4.11.0/",
            "https://javadoc.io/doc/org.jetbrains/annotations/23.0.0/",
            "https://siroshun09.github.io/ConfigAPI/",
            "https://siroshun09.github.io/Event4J/3.2.0",
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
                description.set("A Paper plugin that provide virtual chests.")
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
            if (version.toString().endsWith("-SNAPSHOT")) {
                url = uri(stagingDir.resolve("maven-snapshot"))
            } else {
                url = uri(stagingDir.resolve("maven"))
            }
        }
    }
}
