import org.gradle.api.Project

fun getReleaseProperty(project: Project): Boolean? {
    return project.findProperty("box.release")?.toString()?.toBoolean()
}

fun isPublishing(project: Project): Boolean {
    return getReleaseProperty(project) != null
}

fun isReleaseVersion(project: Project): Boolean {
    return getReleaseProperty(project) == true && !project.version.toString().endsWith("-SNAPSHOT")
}
