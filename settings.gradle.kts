pluginManagement {
    includeBuild("build-logic")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "box"
val boxPrefix = rootProject.name

sequenceOf(
    "api",
    "core",
).forEach {
    include("$boxPrefix-$it")
    project(":$boxPrefix-$it").projectDir = file(it)
}

val storagePrefix = "$boxPrefix-storage"

// storage
sequenceOf(
    "api",
    "migrator",

    // implementations
    "database",
    "yaml"
).forEach {
    include(":$storagePrefix-$it")
    project(":$storagePrefix-$it").projectDir = file("./storage/$it")
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

val versionSuffix = "version"

// versions
sequenceOf(
    "common",
).forEach {
    include("$boxPrefix-$versionSuffix-$it")
    project(":$boxPrefix-$versionSuffix-$it").projectDir = file("./versions/$it")
}

include("$boxPrefix-bundle")
project(":$boxPrefix-bundle").projectDir = file("bundle")
