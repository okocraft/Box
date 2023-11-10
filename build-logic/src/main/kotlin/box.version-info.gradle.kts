import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*

val dir = layout.buildDirectory.dir("generated/version-info").get()
val file = dir.file("version.properties").asFile

tasks.register("createVersionInfo") {
    doFirst {
        if (!dir.asFile.mkdirs()) file.delete()
        file.createNewFile()
        collectInfo().store(file.writer(StandardCharsets.UTF_8), null)
    }
}

plugins.withType<JavaPlugin> {
    extensions.getByType<SourceSetContainer>().named(SourceSet.MAIN_SOURCE_SET_NAME) {
        resources.srcDir(dir)
    }
}

fun collectInfo(): Properties {
    val properties = Properties()

    properties.setProperty("version", project.version.toString())
    properties.setProperty("isReleaseVersion", isReleaseVersion(project).toString())
    properties.setProperty("buildDate", System.currentTimeMillis().toString())

    if (rootDir.resolve(".git").exists()) {
        properties.setProperty("commitHash", getLatestCommitHash())
    }

    if (System.getenv().containsKey("GITHUB_ACTIONS")) {
        putGitHubActionsInfo(properties)
    }

    return properties
}

fun getLatestCommitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

fun putGitHubActionsInfo(properties: Properties) {
    properties.setProperty("ci.serviceName", "GitHub Actions")

    properties.setProperty("ci.buildNumber", System.getenv().getOrDefault("GITHUB_RUN_NUMBER", "UNKNOWN"))
    properties.setProperty("ci.runId", System.getenv().getOrDefault("GITHUB_RUN_ID", "UNKNOWN"))
    properties.setProperty("ci.repository", System.getenv().getOrDefault("GITHUB_REPOSITORY", "UNKNOWN"))
}
