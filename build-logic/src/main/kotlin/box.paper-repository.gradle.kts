plugins {
    `java-library`
}

val enableSnapshotRepo = false // For updating Box when a newer Minecraft version is released.

repositories {
    if (enableSnapshotRepo) {
        sequenceOf(
            "https://oss.sonatype.org/content/repositories/snapshots/",
            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        ).forEach {
            maven {
                url = uri(it)
            }
        }
    }

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}
