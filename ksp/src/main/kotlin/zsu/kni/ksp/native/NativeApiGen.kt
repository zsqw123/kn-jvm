package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import zsu.kni.JniApi
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

    override fun prepareBuilder(function: KSFunctionDeclaration): FunSpec.Builder {
        return FunSpec.builder(function.simpleName.asString() + IMPL_POSTFIX)
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
        val thisPart = parameters.thisPart
        if (!function.isStatic()) {
            singleParam("this" to thisPart)
        }
        parameters.params.forEach { singleParam(it) }
    }

    override fun FunSpec.Builder.buildMethodCall(function: KSFunctionDeclaration, parameters: ApiParameters) {
        TODO("Not yet implemented")
    }

    override fun FunSpec.Builder.buildReturn(function: KSFunctionDeclaration, parameters: ApiParameters) {
        TODO("Not yet implemented")
    }

    /** covert native object to jobject */
    private fun FunSpec.Builder.singleParam(param: ApiParamRecord) {
        val (oldName, newParam) = param
        val typeName = newParam.type
        val newName = newParam.name
        // built in types
        if (typeName in directMappingJniNames) {
            return addVal(newName, oldName)
        }
        if (typeName == BOOLEAN) {
            // boolean is UByte in KN
            return addVal(newName, "if ($oldName) 1u else 0u")
        }
        if (typeName == CHAR) {
            // char is UShort in KN
            return addVal(newName, "$oldName.code.toUShort()")
        }
        // c object -> jobject
        addVal(
            newName, "$NAME_BRIDGE.${nativeNames.c2j}<%T>($oldName, %S, %S)",
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
