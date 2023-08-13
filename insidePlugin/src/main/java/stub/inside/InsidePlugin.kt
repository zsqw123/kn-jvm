package stub.inside

import org.gradle.api.Plugin
import org.gradle.api.Project

class InsidePlugin : Plugin<Project> {
    override fun apply(project: Project) = Unit
}