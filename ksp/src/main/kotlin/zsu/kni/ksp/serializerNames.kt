package zsu.kni.ksp

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

private val ClassName.serializerNameC: String
    get() {
        var main = canonicalName.replace('.', '/').mangled()
        if (isNullable) main += "_nullable"
        return main
    }

private val ParameterizedTypeName.serializerNameP: String
    get() = buildString {
        append('p')
        append(rawType.serializerName)
        append("s_")
        typeArguments.joinTo(this, separator = "_") {
            it.serializerName
        }
        append("_e")
        if (isNullable) append("_nullable")
    }

private val LambdaTypeName.serializerNameL: String
    get() = buildString {
        append('l')
        if (isSuspending) append("suspend_")
        val receiver = receiver
        if (receiver != null) {
            append(receiver.serializerName)
            append('_')
        }
        append("s_")
        parameters.joinTo(this, separator = "_") {
            it.type.serializerName
        }
        append("_e")
        append('_')
        append(returnType.serializerName)
    }
val TypeName.serializerName: String
    get() = when (this@serializerName) {
        is ClassName -> serializerNameC
        is LambdaTypeName -> serializerNameL
        is ParameterizedTypeName -> serializerNameP
        else -> throw UnsupportedOperationException("unsupported type: ${this@serializerName}")
    }

inline val ClassName.internalName get() = canonicalName.replace('.', '/')