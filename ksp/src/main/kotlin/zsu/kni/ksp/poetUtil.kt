package zsu.kni.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName

// copy from kotlin poet, it is internal in poet sadly :(
internal fun TypeName.rawType(): ClassName {
    return findRawType() ?: throw IllegalArgumentException("Cannot get raw type from $this")
}

internal fun TypeName.findRawType(): ClassName? {
    return when (this) {
        is ClassName -> this
        is ParameterizedTypeName -> rawType
        else -> null
    }
}

fun KSFunctionDeclaration.asMemberName(): MemberName {
    val packageName = packageName.asString()
    val parent = parentDeclaration as? KSClassDeclaration
    return if (parent == null) {
        MemberName(packageName, simpleName.asString())
    }else{
        MemberName(parent.toClassName(), simpleName.asString())
    }
}

val optInExpNativeApiAnnotation = AnnotationSpec.builder(
    ClassName("kotlin", "OptIn")
).addMember(
    "%T::class",
    ClassName("kotlinx.cinterop", "ExperimentalForeignApi")
).addMember(
    "%T::class",
    ClassName("kotlin.experimental", "ExperimentalNativeApi")
).build()
