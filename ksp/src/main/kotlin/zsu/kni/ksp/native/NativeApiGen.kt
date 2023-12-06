package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import zsu.kni.JniApi
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.addVal
import zsu.kni.ksp.isStatic
import zsu.kni.ksp.optAnnotation

class NativeApiGen(
    private val context: KniContext
) : NativeFunctionGenByPackage(context) {
    override val generatedFileName: String = C_API

    private val getEnvMethod = MemberName(
        env.envStoreClassName, "get"
    )

    override fun singleFunction(function: KSFunctionDeclaration): FunSpec {
        val envId = function.envId()
        val jenvClassName = nativeNames.jenvPtr
        val builder = FunSpec.builder(function.simpleName.asString() + IMPL_POSTFIX)
        val isStatic = function.isStatic()
        builder.memScoped {
            // get bridge
            addVal("_jenv", jenvClassName, "%M($envId)")
            addVal("_proto", "%T(_jenv, this)", env.protoClassName)
            addVal(NAME_BRIDGE, "%T(_proto)", nativeNames.nativeBridge)
            // build all params

        }

    }

    private fun FunSpec.Builder.buildAllParams(
        function: KSFunctionDeclaration,
        apiParameters: ApiParameters,
    ) {
        require(function.extensionReceiver == null) {
            "call extension function from native to java is not support yet."
        }
        val thisPart = apiParameters.thisPart
        if (thisPart != null) {
            singleParam(thisPart)
        }
        apiParameters.params.forEach { singleParam(it) }
    }

    private fun FunSpec.Builder.singleParam(param: ParameterSpec) {
        val typeName = param.type
        val paramName = param.name
        val realName = "_$paramName"
        // built in types
        if (simpleTypeName in directMappingJniNames) {
            addVal(realName, paramName)
            return
        }
    }

    private fun FunSpec.Builder.buildCall() {

    }


    private fun KSFunctionDeclaration.envId(): Int {
        return requireNotNull(optAnnotation<JniApi>()?.threadId) {
            "must specify thread id for call java from native, function: ${this.qualifiedName}"
        }
    }
}

private const val IMPL_POSTFIX = "Impl"
private const val C_API = "_kni_api"
private const val NAME_BRIDGE = "_bridge"
