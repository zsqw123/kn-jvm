import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

private val Project.kotlin: KotlinMultiplatformExtension
    get() = extensions.getByType(KotlinMultiplatformExtension::class.java)

fun Project.configKmmTargets(vararg targetPlatforms: String) = kotlin.apply {
    val targetPresets = presets.matching { it.name in targetPlatforms }
    targetPresets.forEach {
        if (targets.findByName(it.name) == null) {
            targetFromPreset(it)
        }
    }
    sourceSets.apply {
        val commonMain = getByName("commonMain")
        val commonTest = getByName("commonTest")
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
    }
}
