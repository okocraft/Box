plugins {
    id("box.common-conventions")
}

repositories {
    maven {
        url = uri("https://maven.playpro.com/")
    }
}

dependencies {
    compileOnly(projects.boxApi)
    compileOnly(projects.boxGuiFeature)
    compileOnly(libs.coreprotect)

    testImplementation(projects.boxApi)
    testImplementation(projects.boxGuiFeature)
    testImplementation(projects.boxTestSharedClasses)
}

tasks.javadoc {
    include("net/okocraft/box/feature/autostore/AutoStoreFeature.java")
    include("net/okocraft/box/feature/autostore/AutoStoreSettingProvider.java")
    include("net/okocraft/box/feature/autostore/package-info.java")
    include("net/okocraft/box/feature/autostore/setting/**")
}

afterEvaluate {
    collector.JavadocAggregator.addProject(this)
}
