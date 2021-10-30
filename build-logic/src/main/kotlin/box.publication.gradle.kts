import java.util.Locale

plugins {
    `java-library`
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks {
    javadoc {
        val opts = options as StandardJavadocDocletOptions

        opts.encoding= Charsets.UTF_8.name()
        opts.links(
            "https://papermc.io/javadocs/paper/1.17/",
            "https://jd.adventure.kyori.net/api/4.9.2/",
            "https://javadoc.io/doc/org.jetbrains/annotations/22.0.0/",
            "https://siroshun09.github.io/ConfigAPI/",
            "https://siroshun09.github.io/Event4J/",
            "https://siroshun09.github.io/TranslationLoader/"
        )
        opts.bottom = "<![CDATA[<p class=\"legalCopy\">Copyright &#169;2020-2021 <a href=\"https://github.com/okocraft\">OKOCRAFT</a>.</p>]]>"
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
            // change to point to your repo, e.g. http://my.org/repo
            if (version.toString().endsWith("-SNAPSHOT")) {
                url =  uri(stagingDir.resolve("maven-snapshot"))
            } else{
                url = uri(stagingDir.resolve("maven"))
            }
        }
    }
}
