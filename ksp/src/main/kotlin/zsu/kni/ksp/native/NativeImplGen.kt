package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toClassName
import zsu.kni.internal.JniTypeName
import zsu.kni.internal.JvmBytecodeType.*
import zsu.kni.ksp.*

class NativeImplGen(
    private val context: KniContext
) : NativeFunctionGenByPackage(context) {
    override val generatedFileName: String = C_IMPL

    override fun singleFunction(function: KSFunctionDeclaration): FunSpec {
        val jniName = function.getJniName(context)

        val cNameSpec = AnnotationSpec.builder(cNameClassName)
            .addMember("externName = %S", jniName.fullName).build()
        val parameterContainer = ImplParameters.from(context, function)
        val funBuilder = FunSpec.builder("${jniName.ownerName}_${jniName.methodName}")
            .addParameters(parameterContainer.collectAllSpec())
            .addAnnotation(cNameSpec)

        // get returns
        val returnType = function.returnType!!.resolve()
        val returnJniName = returnType.getJniName(context)
        // no need returns for void type
        val needReturns = returnJniName == JniTypeName.VOID

        funBuilder.memScoped {
            // build proto & bridge
            addVal(
                "_proto", "%T(${parameterContainer.jEnvPart.name}, this)",
                env.protoClassName
            )
            addVal(NAME_BRIDGE, "%T(_proto)", nativeNames.nativeBridge)
            // build params
            buildAllParams(function, parameterContainer)
            // call native function call
            buildCall(function, parameterContainer)
            // return value
            if (needReturns) buildReturn(returnType, returnJniName)
        }

        return funBuilder.build()
    }

    private fun FunSpec.Builder.buildAllParams(
        function: KSFunctionDeclaration,
        implParameters: ImplParameters,
    ) {
        if (!implParameters.isStaticCall) {
            val thisClassName = (function.parentDeclaration as KSClassDeclaration).toClassName()
            singleParam(thisClassName to implParameters.thisPart)
        }
        if (implParameters.extensionPart != null && implParameters.extensionClassName != null) {
            singleParam(implParameters.extensionClassName to implParameters.extensionPart)
        }
        implParameters.params.forEach { singleParam(it) }
    }

    private fun FunSpec.Builder.buildCall(
        function: KSFunctionDeclaration,
        implParameters: ImplParameters,
    ) {
        // normal call
        var callCode = implParameters.params.joinToString(
            separator = ", ", prefix = "%M(", postfix = ")",
        ) { "_${it.second.name}" }

        if (implParameters.extensionPart != null) {
            // has extension
            callCode = "_${implParameters.extensionPart.name}.$callCode"
        }

        val isStatic = implParameters.isStaticCall
        if (!isStatic) {
            // non-static, wrap this with {
            beginControlFlow("with(_${implParameters.thisPart.name})")
        }

        addVal(NAME_RETURNED, callCode, function.asMemberName())

        if (!isStatic) {
            // } end wrap with
            endControlFlow()
        }
    }

    private fun FunSpec.Builder.buildReturn(
        returnType: KSType,
        returnJniName: JniTypeName,
    ) {
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
            addStatement("return if ($NAME_RETURNED) 1.toUByte() else 0.toUByte()")
            return
        }
        if (returnJniNameName == C.jniName) {
            // char is UShort in KN
            addStatement("return $NAME_RETURNED.code.toUShort()")
            return
        }
        addStatement(
            "return $NAME_BRIDGE.c2jType<%T>($NAME_RETURNED, %S, %S)",
            returnTypeName, env.serializerInternalName, returnTypeName.serializerName,
        )
    }

    private fun FunSpec.Builder.singleParam(parameter: Pair<ClassName, ParameterSpec>) {
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
        if (simpleTypeName == B.jniName) {
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
            realName, "$NAME_BRIDGE.j2cType<%T>($paramName, %S, %S)",
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

