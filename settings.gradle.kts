pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "box"

sequenceOf(
    "api",
    "core",
).forEach {
    include(it)
    project(":$it").projectDir = file(it)
}

// features
sequenceOf(
    "autostore",
    "category",
    "command",
    "craft",
    "gui",
    "notifier",
    "stick"
).forEach {
    include(it)
    project(":$it").projectDir = file("./features/$it")
}

include("bundle")
project(":bundle").projectDir = file("bundle")
