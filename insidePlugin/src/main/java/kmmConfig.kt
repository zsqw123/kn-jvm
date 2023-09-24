import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetPreset
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import java.io.File

internal val Project.kme: KotlinMultiplatformExtension
    get() = extensions.getByType(KotlinMultiplatformExtension::class.java)

val Project.jniSourceRoot: File get() = File(buildDir, "generated/jniLibs")

private val deprecatedNativePresets = KonanTarget.deprecatedTargets
    .map { it.presetName }.toSet()

private val allValidNativePresetNames = KonanTarget.predefinedTargets.values
    .map { it.presetName }
    .filterNot { it in deprecatedNativePresets }.toSet()

val Project.allNativePresets: Array<String>
    get() = kme.presets
        .filterIsInstance<KotlinNativeTargetPreset>()
        .filter { it.name in allValidNativePresetNames }
        .mapArray { it.name }

val Project.allJvmPresets: Array<String>
    get() = kme.presets
        .filterIsInstance<KotlinJvmTargetPreset>()
        .mapArray { it.name }

private inline fun <T, reified R> List<T>.mapArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(get(it)) }
}

fun Project.configKmmSourceSet(vararg targetPlatforms: String) = kme.apply {
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
        val nativeMain = create("nativeMain").apply {
            dependsOn(commonMain)
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
