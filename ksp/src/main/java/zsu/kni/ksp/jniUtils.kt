@file:Suppress("SpellCheckingInspection")

package zsu.kni.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument

fun Resolver.typeFromName(
    name: KSName, typeArguments: List<KSTypeArgument> = emptyList()
): KSType {
    return getClassDeclarationByName(name)!!.asType(typeArguments)
}

// eg: jint/jboolean...
@JvmInline
value class JniTypeName(val name: String) {
    companion object {
        val VOID = JniTypeName("void")
    }
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
private fun String.mangled(): String = buildString {
    for (c in this@mangled) {
        when (c) {
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
}

fun KSType.getJniName(resolver: Resolver, mapJavaToKt: Boolean = true): JniTypeName? {
    val buildInTypes = KtBuildInTypes.getInstance(resolver)
    if (this == buildInTypes.voidType) return JniTypeName.VOID

    // map java type to kotlin
    val declaration = declaration
    if (mapJavaToKt && declaration.packageName.asString().startsWith("java")) {
        val name = declaration.qualifiedName!!
        @OptIn(KspExperimental::class) val kotlinName = resolver.mapJavaNameToKotlin(name)!!
        val kotlinType = resolver.typeFromName(kotlinName)
        return kotlinType.getJniName(resolver, false)
    }

    val jniTypeNameText = with(buildInTypes) {
        when (this@getJniName) {
            nothingType, unitType -> return JniTypeName.VOID
            stringType -> "jstring"
            byteType -> "jbyte"
            shortType -> "jshort"
            intType -> "jint"
            longType -> "jlong"
            floatType -> "jfloat"
            doubleType -> "jdouble"
            charType -> "jchar"
            booleanType -> "jboolean"
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
            else -> "jobject"
        }
    }

    return JniTypeName(jniTypeNameText)
}

class KtBuildInTypes(private val resolver: Resolver) : KSBuiltIns by resolver.builtIns {
    val voidType = type<Void>()
    val throwableType = type<Throwable>()

    val byteArray = type<ByteArray>()
    val charArray = type<CharArray>()
    val shortArray = type<ShortArray>()
    val intArray = type<IntArray>()
    val longArray = type<LongArray>()
    val floatArray = type<FloatArray>()
    val doubleArray = type<DoubleArray>()
    val booleanArray = type<BooleanArray>()

    private inline fun <reified T> type(): KSType {
        val requiredName = resolver.getClassDeclarationByName<T>()!!
        return requiredName.asType(emptyList())
    }

    companion object {
        private var buildInTypes: KtBuildInTypes? = null
        fun getInstance(resolver: Resolver): KtBuildInTypes = buildInTypes ?: synchronized(this) {
            KtBuildInTypes(resolver).also {
                buildInTypes = it
            }
        }
    }
}

