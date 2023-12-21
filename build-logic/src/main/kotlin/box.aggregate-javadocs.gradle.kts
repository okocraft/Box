import collector.JavadocAggregator

tasks {
    create<Javadoc>(JavadocAggregator.AGGREGATE_JAVADOC_TASK_NAME) {
        setDestinationDir(layout.buildDirectory.dir("docs").get().asFile)
        classpath = objects.fileCollection()
        doFirst {
            include(JavadocAggregator.includes)
            exclude(JavadocAggregator.excludes)

            (options as StandardJavadocDocletOptions)
                .docTitle("Box $version")
                .windowTitle("Box $version")
                .links(*JavadocAggregator.javadocLinks.toTypedArray())
        }
    }
}
