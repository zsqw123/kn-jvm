package zsu.kni.ksp.template

import org.intellij.lang.annotations.Language

fun interface Template {
    @Language("kotlin")
    fun create(
        packageName: String,
        simpleClassName: String,
        jniPackageName: String
    ): String
}
