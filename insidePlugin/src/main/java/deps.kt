import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.plugin.HasKotlinDependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

@Suppress("ConstPropertyName")
object D {
    const val ksp = "com.google.devtools.ksp:symbol-processing-api:1.9.0-1.0.11"
    const val poet = "com.squareup:kotlinpoet:1.14.2"
    const val pb = "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.0"
}

fun Project.jvmTestDeps(configurationName: String = "testImplementation") {
    dependencies.apply {
        add(configurationName, platform("org.junit:junit-bom:5.9.1"))
        add(configurationName, "org.junit.jupiter:junit-jupiter")
    }
}

fun DependencyHandler.kspCommon(dependencyPath: String) {
    addDeps("kspCommonMainMetadata", dependencyPath)
}

fun DependencyHandler.kspJvm(dependencyPath: String) {
    addDeps("kspJvm", dependencyPath)
}

fun DependencyHandler.kspMingwX64(dependencyPath: String) {
    addDeps("kspMingwX64", dependencyPath)
}

/**
 * @param dependencyPath maven artifact full-id or project path start with `:`
 */
fun DependencyHandler.addDeps(configurationName: String, dependencyPath: String) {
    if (dependencyPath.startsWith(":")) {
        add(configurationName, project(mapOf("path" to dependencyPath)))
    } else {
        add(configurationName, dependencyPath)
    }
}

fun Project.commonMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    val commonMainSourceSet = kotlin.sourceSets.named("commonMain") as HasKotlinDependencies
    commonMainSourceSet.dependencies(configure)
}
