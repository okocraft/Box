import collector.JavadocAggregator

tasks {
    create<Javadoc>(JavadocAggregator.AGGREGATE_JAVADOC_TASK_NAME) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        setDestinationDir(layout.buildDirectory.dir("docs").get().asFile)
        classpath = objects.fileCollection()
        doFirst {
            include(JavadocAggregator.includes)
            exclude(JavadocAggregator.excludes)

            val opts = options as StandardJavadocDocletOptions
            opts.docTitle("Box $version")
                .windowTitle("Box $version")
                .links(*JavadocAggregator.javadocLinks.toTypedArray())

            opts.addStringOption("Xmaxwarns", Int.MAX_VALUE.toString())
        }
    }
}
