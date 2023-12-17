package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.getJniName
import zsu.kni.ksp.isStatic

class ImplParameters(
    val isStaticCall: Boolean,
    val jEnvPart: ParameterSpec,
    val thisPart: ParameterSpec, // can be jclass or jobject
    val extensionClassName: TypeName?, // can be null if not exists
    val extensionPart: ParameterSpec?,
    // originClassTypeName to ParameterSpec
    val params: List<Pair<TypeName, ParameterSpec>>,
) : Parameters {
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

    companion object {
        fun from(context: KniContext, function: KSFunctionDeclaration): ImplParameters {
            return fromInternal(context, function)
        }
    }
}

private fun fromInternal(context: KniContext, function: KSFunctionDeclaration): ImplParameters {
    val nativeNames = context.envContext.nativeNames
    val parameters = function.parameters
    // jenv
    val jEnvPart = ParameterSpec("jenv", nativeNames.jenvPtr)
    // this
    val isStatic = function.isStatic()
    val thisName = if (isStatic) nativeNames.jClass else nativeNames.jObject
    val parentClass = function.parentDeclaration as? KSClassDeclaration
    val thisPart = if (parentClass != null) {
        ParameterSpec("_thisRef", thisName)
    } else {
        // use thisName as stub, but could never access it
        ParameterSpec("_thisRef", thisName)
    }
    // extension
    val extensionType = function.extensionReceiver?.resolve()
    var extensionClassName: TypeName? = null
    var extensionPart: ParameterSpec? = null
    if (extensionType != null) {
        extensionClassName = extensionType.toTypeName()
        extensionPart = ParameterSpec(
            "_extension", nativeNames.jni(extensionType.getJniName(context))
        )
    }
    // params
    val params = ArrayList<Pair<TypeName, ParameterSpec>>(parameters.size)
    for (parameter in parameters) {
        val paramType = parameter.type.resolve()
        val paramTypeName = paramType.toTypeName()
        val paramTypeJniName = paramType.getJniName(context)
        params += paramTypeName to ParameterSpec(
            "p_${parameter.name!!.asString()}", nativeNames.jni(paramTypeJniName)
        )
    }
    return ImplParameters(
        isStatic, jEnvPart, thisPart, extensionClassName, extensionPart, params,
    )
}
