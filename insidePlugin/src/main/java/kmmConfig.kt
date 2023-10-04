import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import java.io.File

val Project.jniSourceRoot: File get() = File(buildDir, "generated/jniLibs")

private val canRunJvmFamilies = arrayOf(
    Family.OSX, Family.MINGW, Family.LINUX,
    Family.ANDROID, // actually android also linux :)
)

private val canRunJvmPresets = KonanTarget.predefinedTargets.values.filter {
    it.family in canRunJvmFamilies
}

private val allNeededNativePresetNames = canRunJvmPresets
    .filterNot { it in KonanTarget.deprecatedTargets }
    .map { it.presetName }.toSet()

val Project.neededNativePresets: Array<String>
    get() = kme.presets
        .filterIsInstance<AbstractKotlinNativeTargetPreset<*>>()
        .filter { it.name in allNeededNativePresetNames }
        .mapArray { it.name }

val jvmPreset: String get() = "jvm"

fun Project.configKmmSourceSet(vararg targetPlatforms: String) = kme.apply {
    val targets = targets
    val targetPresets = presets.matching { it.name in targetPlatforms }
    targetPresets.forEach {
        if (targets.findByName(it.name) == null) {
            targetFromPreset(it)
        }
    }
    sourceSets.apply {
        val commonMain = getByName("commonMain")
        val commonTest = getByName("commonTest") {
            dependsOn(commonMain)
        }
        val nativeMain = create("nativeMain").apply {
            dependsOn(commonMain)
        }
        targets.withType(KotlinNativeTarget::class.java) {
            compilations.apply {
                getByName("main").defaultSourceSet.dependsOn(nativeMain)
                getByName("test").defaultSourceSet.dependsOn(commonTest)
            }
        }

        targets.withType(KotlinJvmTarget::class.java) {
            val resSourceSet = compilations.getByName("main").defaultSourceSet.resources
            resSourceSet.srcDir(File(buildDir, "generated/jniLibs"))
            compilations.all {
                kotlinOptions.jvmTarget = JvmTarget.JVM_1_8.target
            }
        }
    }
}

fun Project.addsKspDependsOn(vararg targetPlatforms: String) = kme.apply {
    afterEvaluate {
        val allKotlinSourceSet = sourceSets
        val (allGenerated, allNotGenerated) = allKotlinSourceSet.partition {
            it.name.contains("generatedBy", true)
        }
        for (targetPlatform in targetPlatforms) {
            val kspSourceSet = allGenerated.firstOrNull {
                it.name.contains(targetPlatform, true)
            }
            val mainSourceSetName = "${targetPlatform}Main"
            val mainSourceSet = allNotGenerated.firstOrNull {
                mainSourceSetName == it.name
            }
            if (kspSourceSet != null && mainSourceSet != null) {
                kspSourceSet.dependsOn(mainSourceSet)
            }
        }
    }
}
