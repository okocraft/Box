package collector

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType

object JavadocAggregator {

    private const val COLLECT_JAVADOC_TASK = "collectJavadocTask"
    const val AGGREGATE_JAVADOC_TASK_NAME = "aggregateJavadoc"

    val javadocLinks: MutableSet<String> = HashSet()
    val includes: MutableSet<String> = HashSet()
    val excludes: MutableSet<String> = HashSet()

    fun addProject(project: Project) {
        val javadocTask = project.tasks.withType<Javadoc>()[JavaPlugin.JAVADOC_TASK_NAME]
        val collectTask = project.tasks.create(COLLECT_JAVADOC_TASK)
        val javadocClasspath = project.files().builtBy(collectTask)

        collectTask.doFirst {
            javadocClasspath.from(javadocTask.classpath.files).builtBy(javadocTask.classpath)
            includes.addAll(javadocTask.includes)
            excludes.addAll(javadocTask.excludes)
            (javadocTask.options as StandardJavadocDocletOptions).links?.let { javadocLinks.addAll(it) }
        }

        project.rootProject.tasks.named<Javadoc>(AGGREGATE_JAVADOC_TASK_NAME) {
            dependsOn(project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME))
            source(javadocTask.source)
            (classpath as ConfigurableFileCollection).from(javadocClasspath)
        }
    }
}
