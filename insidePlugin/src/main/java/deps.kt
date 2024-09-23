import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

@Suppress("ConstPropertyName")
object D {
    const val ksp = "com.google.devtools.ksp:symbol-processing-api:2.0.20-1.0.25"
    private const val poetVersion = "1.14.2"
    const val poet = "com.squareup:kotlinpoet:$poetVersion"
    const val poetKsp = "com.squareup:kotlinpoet-ksp:$poetVersion"
    const val pb = "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.0"

    const val junitBom = "org.junit:junit-bom:5.9.1"
    const val junitJupiter = "org.junit.jupiter:junit-jupiter"
    const val coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
}

fun DependencyHandler.jvmTestDeps(configurationName: String = "testImplementation") {
    add(configurationName, platform(D.junitBom))
    add(configurationName, D.junitJupiter)
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
private fun DependencyHandler.addDeps(configurationName: String, dependencyPath: String) {
    if (dependencyPath.startsWith(":")) {
        add(configurationName, project(mapOf("path" to dependencyPath)))
    } else {
        add(configurationName, dependencyPath)
    }
}

fun Project.commonMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kme.sourceSets.named("commonMain").configure {
        dependencies(configure)
    }
}
