plugins {
    id("box.common-conventions")
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxCategoryFeature)
}
