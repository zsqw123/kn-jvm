import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

fun Project.configJniInterop(
    target: KotlinNativeTarget,
    jniCInteropName: String,
    jniPackageName: String,
) {
    target.compilations.getByName("main").cinterops.getOrCreate(jniCInteropName) {
        val javaHome = File(System.getProperty("java.home"))
        it.packageName = jniPackageName
        it.includeDirs.allHeadersDirs += files(
            File(javaHome, "include"),
            File(javaHome, "include/darwin"),
            File(javaHome, "include/linux"),
            File(javaHome, "include/win32")
        )
    }
}

fun Project.addsNativeLibToJniSources(library: SharedLibrary): KotlinNativeLink {
    val linkTask = library.linkTaskProvider.get()
    linkTask.doLast {
        copy {
            from(library.outputFile)
            into(jniSourceRoot)
        }
    }
    return linkTask
}

private fun <T> NamedDomainObjectContainer<T>.getOrCreate(name: String, createAction: (T) -> Unit) {
    if (findByName(name) == null) create(name, createAction)
}
