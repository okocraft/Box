pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "box"
val boxPrefix = rootProject.name

sequenceOf(
    "api",
    "core",
).forEach {
    include("$boxPrefix-$it")
    project(":$boxPrefix-$it").projectDir = file(it)
}

val featureSuffix = "feature"

// features
sequenceOf(
    "autostore",
    "bemode",
    "category",
    "command",
    "craft",
    "gui",
    "notifier",
    "stick"
).forEach {
    include("$boxPrefix-$it-$featureSuffix")
    project(":$boxPrefix-$it-$featureSuffix").projectDir = file("./features/$it")
}

include("$boxPrefix-bundle")
project(":$boxPrefix-bundle").projectDir = file("bundle")
