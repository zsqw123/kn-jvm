import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.File

private val Project.kotlin: KotlinMultiplatformExtension
    get() = extensions.getByType(KotlinMultiplatformExtension::class.java)

val Project.jniSourceRoot: File get() = File(buildDir, "generated/jniLibs")

fun Project.configKmmTargets(vararg targetPlatforms: String) = kotlin.apply {
    val targetPresets = presets.matching { it.name in targetPlatforms }
    targetPresets.forEach {
        if (targets.findByName(it.name) == null) {
            targetFromPreset(it)
        }
    }
    sourceSets.apply {
        val commonMain = getByName("commonMain")
        val commonTest = getByName("commonTest") {
            it.dependsOn(commonMain)
        }
        val multithreadedMain = create("multithreadedMain").apply {
            dependsOn(commonMain)
        }
        val nativeMain = create("nativeMain").apply {
            dependsOn(multithreadedMain)
        }

        targets.withType(KotlinNativeTarget::class.java) {
            it.compilations.apply {
                getByName("main").defaultSourceSet.dependsOn(nativeMain)
                getByName("test").defaultSourceSet.dependsOn(commonTest)
            }
        }

        targets.withType(KotlinJvmTarget::class.java) {
            it.compilations.apply {
                val resSourceSet = getByName("main").defaultSourceSet.resources
                resSourceSet.srcDir(File(buildDir, "generated/jniLibs"))
            }
        }
    }
}
