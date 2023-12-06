package zsu.kni.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import zsu.kni.JniApi
import zsu.kni.JniImpl
import zsu.kni.JniShared

val jniSharedFqn: String = JniShared::class.java.name
val jniApiFqn: String = JniApi::class.java.name
val jniImplFqn: String = JniImpl::class.java.name

val cNameClassName = ClassName("kotlin.native", "CName")

@OptIn(KspExperimental::class)
fun KSFunctionDeclaration.isStatic(): Boolean {
    return parentDeclaration == null || isAnnotationPresent(JvmStatic::class)
}

fun KSFunctionDeclaration.actualParentClass(): KSClassDeclaration? {
    val parent = parentDeclaration ?: return null
    require(parent is KSClassDeclaration) {
        "parent not a class/object! parent: ${parent.qualifiedName}"
    }
    if (parent.isCompanionObject && isStatic()) {
        return parent.parentDeclaration as? KSClassDeclaration
    }
    return parent
}

fun FunSpec.Builder.addVal(
    paramName: String, initializer: String, vararg args: Any
) {
    addStatement("val $paramName = $initializer", *args)
}

fun FunSpec.Builder.addVal(
    paramName: String, type: TypeName, initializer: String, vararg args: Any
) {
    addStatement("val $paramName: %T = $initializer", type, *args)
}


@OptIn(KspExperimental::class)
inline fun <reified T : Annotation> KSFunctionDeclaration.optAnnotation(): T? {
    return getAnnotationsByType(T::class).firstOrNull()
}
