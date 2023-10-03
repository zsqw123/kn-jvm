@file:Suppress("SpellCheckingInspection")

package zsu.kni.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import zsu.kni.internal.JniTypeName
import zsu.kni.internal.JvmBytecodeType

fun Resolver.typeFromName(
    name: KSName, typeArguments: List<KSTypeArgument> = emptyList()
): KSType {
    return getClassDeclarationByName(name)!!.asType(typeArguments)
}

// eg: Java_com_foo_BarKt_something
data class JniFuncName(
    val packageName: String, val ownerName: String, val methodName: String
) {
    val fullName = "Java_${packageName}_${ownerName}_$methodName"
}

@OptIn(KspExperimental::class)
fun KSFunctionDeclaration.getJniName(context: KniContext): JniFuncName {
    val resolver = context.resolver
    val packageName = packageName.asString().replace('.', '/').mangled()
    val ownerClassName = resolver.getOwnerJvmClassName(this@getJniName)!!.mangled()
    val methodName = resolver.getJvmName(this@getJniName)!!.mangled()
    return JniFuncName(packageName, ownerClassName, methodName)
}

// List<originClassName to ParameterSpec>
fun KSFunctionDeclaration.parameterSpecs(context: KniContext): ParameterSpecContainer {
    val nativeNames = context.envContext.nativeNames
    val parameters = parameters
    // jenv
    val jEnvPart = ParameterSpec("jenv", nativeNames.jenvPtr)
    // this
    val isStatic = isStatic()
    val thisName = if (isStatic) nativeNames.jClass else nativeNames.jObject
    val parentClass = parentDeclaration as? KSClassDeclaration
    val thisPart = if (parentClass != null) {
        ParameterSpec("_thisRef", thisName)
    } else {
        // use thisName as stub, but could never access it
        ParameterSpec("_thisRef", thisName)
    }
    // extension
    val extensionType = extensionReceiver?.resolve()
    var extensionClassName: ClassName? = null
    var extensionPart: ParameterSpec? = null
    if (extensionType != null) {
        extensionClassName = extensionType.toClassName()
        extensionPart = ParameterSpec(
            "_extension", nativeNames.jni(extensionType.getJniName(context))
        )
    }
    // params
    val params = ArrayList<Pair<ClassName, ParameterSpec>>(parameters.size)
    for (parameter in parameters) {
        val paramType = parameter.type.resolve()
        val paramTypeName = paramType.toClassName()
        val paramTypeJniName = paramType.getJniName(context)
        params += paramTypeName to ParameterSpec(
            "p_${parameter.name!!.asString()}", nativeNames.jni(paramTypeJniName)
        )
    }
    return ParameterSpecContainer(
        isStatic, jEnvPart, thisPart, extensionClassName, extensionPart, params,
    )
}

class ParameterSpecContainer(
    val isStaticCall: Boolean,
    val jEnvPart: ParameterSpec,
    val thisPart: ParameterSpec, // can be jclass or jobject
    val extensionClassName: ClassName?, // can be null if not exists
    val extensionPart: ParameterSpec?,
    // originClassTypeName to ParameterSpec
    val params: List<Pair<ClassName, ParameterSpec>>,
) {
    init {
        if (extensionPart != null) require(extensionClassName != null)
    }

    fun collectAllSpec(): List<ParameterSpec> {
        var size = 2 + params.size
        if (extensionPart != null) size++
        val result = ArrayList<ParameterSpec>(size)
        result += jEnvPart
        result += thisPart
        if (extensionPart != null) result += extensionPart
        result += params.map { it.second }
        return result
    }
}


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

private val voidClassName = Void::class.asClassName()
fun KSType.getJniName(context: KniContext, mapJavaToKt: Boolean = true): JniTypeName {
    val buildInTypes = context.buildInTypes
    val resolver = context.resolver
    val className = this.toClassName()
    if (className == voidClassName) return JniTypeName.VOID

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

private val buildInSerializerTypes = JvmBytecodeType.entries.filter {
    it != JvmBytecodeType.V && it != JvmBytecodeType.L
}.map { it.jniName }

fun KSType.isCustomSerializerNeeded(context: KniContext): Boolean {
    val jniName = getJniName(context).jniName
    return jniName !in buildInSerializerTypes
}
