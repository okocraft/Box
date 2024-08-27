extensions.add(
    "boxBuildProperties",
    BoxBuildProperties(
        isPublishing(project),
        isReleaseVersion(project)
    )
)

data class BoxBuildProperties(val isPublishing: Boolean, val isReleaseVersion: Boolean)
