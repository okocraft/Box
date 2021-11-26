plugins {
    id("io.freefair.aggregate-javadoc") version "6.2.0"
}

tasks {
    aggregateJavadoc {
        sequenceOf(
            "api/**",
            "feature/autostore/event/AutoStoreSettingChangeEvent.java",
            "feature/autostore/model/**",
            "feature/category/CategoryHolder.java",
            "feature/category/model/Category.java",
            "feature/gui/api/**"
        ).forEach { include("net/okocraft/box/$it") }

        (options as StandardJavadocDocletOptions).docTitle("Box-$version")
    }
}
