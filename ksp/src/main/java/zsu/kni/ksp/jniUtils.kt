@file:Suppress("SpellCheckingInspection")

package zsu.kni.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import zsu.kni.internal.JniTypeName
import zsu.kni.internal.JvmBytecodeType

fun Resolver.typeFromName(
    name: KSName, typeArguments: List<KSTypeArgument> = emptyList()
): KSType {
    return getClassDeclarationByName(name)!!.asType(typeArguments)
}

// eg: Java_com_foo_BarKt_something
@JvmInline
value class JniFuncName(val name: String)

fun KSFunctionDeclaration.getJniName(resolver: Resolver): JniFuncName = buildString {
    val packageName = packageName.asString().replace('.', '/')
    append("Java_")
    append(packageName.mangled())

    append('_')
    @OptIn(KspExperimental::class)
    val ownerClassName = resolver.getOwnerJvmClassName(this@getJniName)!!
    append(ownerClassName.mangled())

    append('_')
    @OptIn(KspExperimental::class)
    val methodName = resolver.getJvmName(this@getJniName)!!
    append(methodName.mangled())
}.let { JniFuncName(it) }

/**
 * [JNI Design Spec](https://docs.oracle.com/en/java/javase/17/docs/specs/jni/design.html#resolving-native-method-names)
 */
fun String.mangled(): String = buildString {
    for (c in this@mangled) when (c) {
        '/' -> append("_")
        '_' -> append("_1")
        ';' -> append("_2")
        '[' -> append("_3")
        in '0'..'9', in 'a'..'z', in 'A'..'Z' -> append(c)
        else -> {
            append("_0")
            append(String.format("%04x", c.code))
        }
    }
}

fun KSType.getJniName(context: KniContext, mapJavaToKt: Boolean = true): JniTypeName? {
    val buildInTypes = context.buildInTypes
    val resolver = context.resolver
    if (this == buildInTypes.voidType) return JniTypeName.VOID

    // map java type to kotlin
    val declaration = declaration
    if (mapJavaToKt && declaration.packageName.asString().startsWith("java")) {
        val name = declaration.qualifiedName!!
        @OptIn(KspExperimental::class) val kotlinName = resolver.mapJavaNameToKotlin(name)!!
        val kotlinType = resolver.typeFromName(kotlinName)
        return kotlinType.getJniName(context, false)
    }

    val jniTypeNameText = with(buildInTypes) {
        val basicType = when (this@getJniName) {
            nothingType, unitType -> JvmBytecodeType.V
            byteType -> JvmBytecodeType.B
            shortType -> JvmBytecodeType.S
            intType -> JvmBytecodeType.I
            longType -> JvmBytecodeType.J
            floatType -> JvmBytecodeType.F
            doubleType -> JvmBytecodeType.D
            charType -> JvmBytecodeType.C
            booleanType -> JvmBytecodeType.Z
            else -> null
        }
        if (basicType != null) return JniTypeName(basicType.jniName)
        when (this@getJniName) {
            stringType -> "jstring"

            // arrays
            byteArray -> "jbyteArray"
            charArray -> "jcharArray"
            shortArray -> "jshortArray"
            intArray -> "jintArray"
            longArray -> "jlongArray"
            floatArray -> "jfloatArray"
            doubleArray -> "jdoubleArray"
            booleanArray -> "jbooleanArray"
            arrayType -> "jobjectArray"

            throwableType -> "jthrowable"
            else -> JvmBytecodeType.L.jniName
        }
    }

    return JniTypeName(jniTypeNameText)
}


