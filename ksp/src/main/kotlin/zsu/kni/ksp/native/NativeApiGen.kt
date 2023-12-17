package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import zsu.kni.JniApi
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.addVal
import zsu.kni.ksp.isStatic
import zsu.kni.ksp.optAnnotation

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
            singleParam(thisPart)
        }
        parameters.params.forEach { singleParam(it) }
    }

    override fun FunSpec.Builder.buildMethodCall(function: KSFunctionDeclaration, parameters: ApiParameters) {
        TODO("Not yet implemented")
    }

    override fun FunSpec.Builder.buildReturn(function: KSFunctionDeclaration, parameters: ApiParameters) {
        TODO("Not yet implemented")
    }

    private fun FunSpec.Builder.singleParam(param: ParameterSpec) {
        val typeName = param.type
        val paramName = param.name
        // built in types
        if (typeName.isPrimitive()) {
            addVal(paramName, typeName, "origin") // todo
            return
        }
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

private fun TypeName.isPrimitive(): Boolean = this in directMappingJniNames

private const val IMPL_POSTFIX = "Impl"
private const val C_API = "_kni_api"
private const val NAME_BRIDGE = "_bridge"
