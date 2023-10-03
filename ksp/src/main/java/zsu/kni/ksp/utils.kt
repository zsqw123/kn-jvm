package zsu.kni.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
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

inline val ClassName.serializerName: String
    get() = canonicalName.replace('.', '/').mangled()
