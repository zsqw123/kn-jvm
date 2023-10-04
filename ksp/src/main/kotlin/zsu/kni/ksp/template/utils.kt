package zsu.kni.ksp.template

import org.intellij.lang.annotations.Language

@Language("kotlin")
@Suppress("NOTHING_TO_INLINE")
inline fun kt(@Language("kotlin") content: String): String = content