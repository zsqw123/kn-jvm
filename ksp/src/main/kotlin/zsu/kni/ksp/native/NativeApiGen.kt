package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.MemberName.Companion.member
import zsu.kni.JniApi
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.ksp.*

/**
 * call jvm from native
 */
class NativeApiGen(
    private val context: KniContext
) : NativeFunctionGenByPackage<ApiParameters>(context) {
    override val generatedFileName: String = C_API

    override fun parametersFromFunction(function: KSFunctionDeclaration): ApiParameters {
        return ApiParameters.from(context, function)
    }

    override fun prepareBuilder(function: KSFunctionDeclaration, parameters: ApiParameters): FunSpec.Builder {
        val originParameters = parameters.params.map {
            ParameterSpec(it.originName, it.paramType)
        }
        return FunSpec.builder(function.simpleName.asString() + IMPL_POSTFIX)
            .addParameters(originParameters)
    }

    override fun FunSpec.Builder.buildNativeBridge(function: KSFunctionDeclaration, parameters: ApiParameters) {
        val envId = function.envId()
        val jenvClassName = nativeNames.jenvPtr
        addVal("_jenv", jenvClassName, "%M($envId)", env.envGetMember)
        addVal("_proto", "%T(_jenv, this)", env.protoClassName)
        addVal(NAME_BRIDGE, "%T(_proto)", nativeNames.nativeBridge)
    }

    override fun FunSpec.Builder.buildAllParams(
        function: KSFunctionDeclaration,
        parameters: ApiParameters,
    ) {
        require(function.extensionReceiver == null) {
            "call extension function from native to java is not support yet."
        }
        if (!function.isStatic()) singleParam(
            ApiParamRecord("this", "_this", parameters.thisPart)
        )
        parameters.params.forEach { singleParam(it) }
    }

    override fun FunSpec.Builder.buildMethodCall(function: KSFunctionDeclaration, parameters: ApiParameters) {
        val args = parameters.params.joinToString(
            separator = ", ", prefix = "listOf(", postfix = ")"
        ) {
            "${it.jvmBytecodeType.asRawMember()} to ${it.newName}"
        }
        addVal("_args", args)
        val bytecodeRaw = parameters.returnBytecodeType.asRawMember()
        val functionName = function.simpleName.asString()
        val methodDesc = requireNotNull(context.optMethodDesc(function)) {
            "get method desc failed: $function"
        }
        if (function.isStatic()) {
            val internalName = parameters.thisPart.internalName
            addVal(
                JVM_RESULT,
                "$NAME_BRIDGE.%N(%S, %S, %S, _args, $bytecodeRaw)",
                nativeNames.callStaticMember,
                internalName, functionName, methodDesc,
            )
        } else {
            val thisJObjName = "_thisJ"
            val thisTypeName = parameters.thisPart
            addVal(
                thisJObjName,
                "$NAME_BRIDGE.${nativeNames.c2j}<%T>($JVM_RESULT, %S, %S)",
                thisTypeName, env.serializerInternalName, thisTypeName.serializerName
            )
            addVal(
                JVM_RESULT,
                "$NAME_BRIDGE.%N($thisJObjName, %S, %S, _args, $bytecodeRaw)",
                nativeNames.callMember, functionName, methodDesc
            )
        }
    }

    override fun FunSpec.Builder.buildReturn(function: KSFunctionDeclaration, parameters: ApiParameters) {
        val returnBytecodeType = parameters.returnBytecodeType
        // no need process for void
        if (returnBytecodeType == JvmBytecodeType.V) return
        val returnTypeName = parameters.returnTypeName
        returns(returnTypeName)
        if (returnTypeName.isNullable) {
            addStatement("$JVM_RESULT ?: return null")
        } else {
            addStatement("$JVM_RESULT!!")
        }
        // basic types
        val realReturnProp = "_$JVM_RESULT"
        var realReturnPropInitializer = "$JVM_RESULT.${returnBytecodeType.jvaluePropName}"
        realReturnPropInitializer += if (returnTypeName.isNullable) " ?: return null" else "!!"
        addVal(realReturnProp, realReturnPropInitializer)
        when (returnBytecodeType) {
            JvmBytecodeType.Z -> addStatement("return $realReturnProp == 1u")
            JvmBytecodeType.C -> addStatement("return $realReturnProp.toInt().toChar()")
            JvmBytecodeType.L -> addStatement(
                // jobject
                "return $NAME_BRIDGE.${nativeNames.j2c}<%T>($realReturnProp, %S, %S)",
                returnTypeName, env.serializerInternalName, returnTypeName.serializerName,
            )

            else -> {}
        }
    }

    /** covert native object to jobject */
    private fun FunSpec.Builder.singleParam(record: ApiParamRecord) {
        val (oldName, newName, typeName) = record
        val bytecodeType = record.jvmBytecodeType
        fun addVal(
            initializer: String, vararg args: Any,
        ) {
            addVal(
                paramName = newName,
                initializer = "$NAME_BRIDGE.${nativeNames.objAsValue}($initializer, %M)",
                *args, bytecodeType.asMember(),
            )
        }

        // built in types
        if (typeName in directMappingJniNames) {
            return addVal(initializer = oldName)
        }
        if (typeName == BOOLEAN) {
            // boolean is UByte in KN
            return addVal(initializer = "if ($oldName) 1u else 0u")
        }
        if (typeName == CHAR) {
            // char is UShort in KN
            return addVal(initializer = "$oldName.code.toUShort()")
        }
        // c object -> jobject
        addVal(
            initializer = "$NAME_BRIDGE.${nativeNames.c2j}<%T>($oldName, %S, %S)",
            typeName, env.serializerInternalName, typeName.serializerName,
        )
    }

    private fun KSFunctionDeclaration.envId(): Int {
        return requireNotNull(optAnnotation<JniApi>()?.threadId) {
            "must specify thread id for call java from native, function: ${this.qualifiedName}"
        }
    }
}

private val directMappingJniNames = setOf(
    BYTE,
    DOUBLE,
    FLOAT,
    INT,
    LONG,
    SHORT,
)

private const val IMPL_POSTFIX = "Impl"
private const val C_API = "_kni_api"
private const val NAME_BRIDGE = "_bridge"
private const val JVM_RESULT = "result" // type: jvalue

private val jvmBytecodeClassName = JvmBytecodeType::class.asClassName()

private fun JvmBytecodeType.asMember(): MemberName {
    return jvmBytecodeClassName.member(name)
}

private fun JvmBytecodeType.asRawMember(): String {
    return "${jvmBytecodeClassName.simpleName}.$name"
}
