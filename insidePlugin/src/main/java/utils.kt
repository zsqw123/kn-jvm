import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal val Project.kme: KotlinMultiplatformExtension
    get() = extensions.getByType(KotlinMultiplatformExtension::class.java)

internal inline fun <T, reified R> List<T>.mapArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(get(it)) }
}
