package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import zsu.kni.internal.JniTypeName
import zsu.kni.internal.JvmBytecodeType.*
import zsu.kni.ksp.*

class NativeImplGen(
    private val context: KniContext
) : NativeFunctionGenByPackage<ImplParameters>(context) {
    override val generatedFileName: String = C_IMPL

    override fun prepareBuilder(function: KSFunctionDeclaration): FunSpec.Builder {
        val jniName = function.getJniName(context)
        val cNameSpec = AnnotationSpec.builder(cNameClassName)
            .addMember("externName = %S", jniName.fullName).build()
        val parameterContainer = ImplParameters.from(context, function)
        return FunSpec.builder("${jniName.ownerName}_${jniName.methodName}")
            .addParameters(parameterContainer.collectAllSpec())
            .addAnnotation(cNameSpec)
    }

    override fun FunSpec.Builder.buildNativeBridge(function: KSFunctionDeclaration, parameters: ImplParameters) {
        addVal(
            "_proto", "%T(${parameters.jEnvPart.name}, this)",
            env.protoClassName
        )
        addVal(NAME_BRIDGE, "%T(_proto)", nativeNames.nativeBridge)
    }

    override fun FunSpec.Builder.buildAllParams(
        function: KSFunctionDeclaration,
        parameters: ImplParameters,
    ) {
        if (!parameters.isStaticCall) {
            val thisClassName = (function.parentDeclaration as KSClassDeclaration).toClassName()
            singleParam(thisClassName to parameters.thisPart)
        }
        if (parameters.extensionPart != null && parameters.extensionClassName != null) {
            singleParam(parameters.extensionClassName to parameters.extensionPart)
        }
        parameters.params.forEach { singleParam(it) }
    }

    override fun FunSpec.Builder.buildMethodCall(
        function: KSFunctionDeclaration,
        parameters: ImplParameters,
    ) {
        // normal call
        var callCode = parameters.params.joinToString(
            separator = ", ", prefix = "%M(", postfix = ")",
        ) { "_${it.second.name}" }

        if (parameters.extensionPart != null) {
            // has extension
            callCode = "_${parameters.extensionPart.name}.$callCode"
        }

        val isStatic = parameters.isStaticCall
        if (!isStatic) {
            // non-static, wrap this with {
            beginControlFlow("with(_${parameters.thisPart.name})")
        }

        addVal(NAME_RETURNED, callCode, function.asMemberName())

        if (!isStatic) {
            // } end wrap with
            endControlFlow()
        }
    }

    override fun FunSpec.Builder.buildReturn(
        function: KSFunctionDeclaration, parameters: ImplParameters
    ) {
        // get returns
        val returnType = function.returnType!!.resolve()
        val returnJniName = returnType.getJniName(context)
        // no need returns for void type
        val needReturns = returnJniName != JniTypeName.VOID

        if (!needReturns) return

        val returnTypeName = returnType.toClassName()
        val returnJniNameName = returnJniName.jniName
        val returnJniClassName = nativeNames.jni(returnJniName)
        returns(returnJniClassName)
        // built in types
        if (returnJniNameName in directMappingJniNames) {
            addStatement("return $NAME_RETURNED")
            return
        }
        if (returnJniNameName == B.jniName) {
            // boolean is UByte in KN
            addStatement("return if ($NAME_RETURNED) 1u else 0u")
            return
        }
        if (returnJniNameName == C.jniName) {
            // char is UShort in KN
            addStatement("return $NAME_RETURNED.code.toUShort()")
            return
        }
        addStatement(
            "return $NAME_BRIDGE.${nativeNames.c2j}<%T>($NAME_RETURNED, %S, %S)",
            returnTypeName, env.serializerInternalName, returnTypeName.serializerName,
        )
    }

    override fun parametersFromFunction(function: KSFunctionDeclaration): ImplParameters {
        return ImplParameters.from(context, function)
    }

    private fun FunSpec.Builder.singleParam(parameter: Pair<TypeName, ParameterSpec>) {
        val (originClassName, parameterSpec) = parameter
        val paramName = parameterSpec.name
        val paramType = parameterSpec.type as ClassName
        val simpleTypeName = paramType.simpleName
        val realName = "_$paramName"
        // built in types
        if (simpleTypeName in directMappingJniNames) {
            addVal(realName, paramName)
            return
        }
        if (simpleTypeName == Z.jniName) {
            // boolean is UByte in KN
            addVal(realName, "$paramName.toInt() == 1")
            return
        }
        if (simpleTypeName == C.jniName) {
            // char is UShort in KN
            addVal(realName, "$paramName.toInt().toChar()")
            return
        }
        // custom type, must be jobject
        require(simpleTypeName == L.jniName) {
            "invalid type: `$simpleTypeName` when process $paramName"
        }
        addVal(
            realName, "$NAME_BRIDGE.${nativeNames.j2c}<%T>($paramName, %S, %S)",
            originClassName, env.serializerInternalName, originClassName.serializerName,
        )
    }
}

private const val C_IMPL = "_kni_impl"
private const val NAME_RETURNED = "returned"
private const val NAME_BRIDGE = "_bridge"

private val directMappingJniNames = setOf(
    B.jniName,
    D.jniName,
    F.jniName,
    I.jniName,
    J.jniName,
    S.jniName,
)
