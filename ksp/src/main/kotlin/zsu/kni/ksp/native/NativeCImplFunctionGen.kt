package zsu.kni.ksp.native

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import zsu.kni.internal.JvmBytecodeType.*
import zsu.kni.ksp.*

class NativeCImplFunctionGen(
    private val context: KniContext
) {
    private val env = context.envContext
    private val nativeNames = env.nativeNames

    private fun singleFunction(function: KSFunctionDeclaration): FunSpec {
        val jniName = function.getJniName(context)

        val cNameSpec = AnnotationSpec.builder(cNameClassName)
            .addMember("externName = %S", jniName.fullName).build()
        val parameterContainer = function.parameterSpecs(context)
        val funBuilder = FunSpec.builder("${jniName.ownerName}_${jniName.methodName}")
            .addParameters(parameterContainer.collectAllSpec())
            .addAnnotation(cNameSpec)
        funBuilder.apply {
            beginControlFlow("%M", nativeNames.memScoped)
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
            buildReturn(function)
            endControlFlow()
        }

        return funBuilder.build()
    }

    private fun FunSpec.Builder.buildAllParams(
        function: KSFunctionDeclaration,
        container: ParameterSpecContainer,
    ) {
        if (!container.isStaticCall) {
            val thisClassName = (function.parentDeclaration as KSClassDeclaration).toClassName()
            singleParam(thisClassName to container.thisPart)
        }
        if (container.extensionPart != null && container.extensionClassName != null) {
            singleParam(container.extensionClassName to container.extensionPart)
        }
        container.params.forEach { singleParam(it) }
    }

    private fun FunSpec.Builder.buildCall(
        function: KSFunctionDeclaration,
        container: ParameterSpecContainer,
    ) {
        // normal call
        var callCode = container.params.joinToString(
            separator = ", ", prefix = "%M(", postfix = ")",
        ) { "_${it.second.name}" }

        if (container.extensionPart != null) {
            // has extension
            callCode = "_${container.extensionPart.name}.$callCode"
        }

        val isStatic = container.isStaticCall
        if (!isStatic) {
            // non-static, wrap this with {
            beginControlFlow("with(_${container.thisPart.name})")
        }

        addVal(NAME_RETURNED, callCode, function.asMemberName())

        if (!isStatic) {
            // } end wrap with
            endControlFlow()
        }
    }

    private fun FunSpec.Builder.buildReturn(
        function: KSFunctionDeclaration,
    ) {
        val returnType = function.returnType!!.resolve()
        val returnTypeName = returnType.toClassName()
        val returnJniName = returnType.getJniName(context)
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

    private fun FunSpec.Builder.addVal(
        paramName: String, initializer: String, vararg args: Any
    ) {
        addStatement("val $paramName = $initializer", *args)
    }

    private fun generateFile(filePackage: String, functions: List<KSFunctionDeclaration>): FileSpec {
        val fileBuilder = FileSpec.builder(filePackage, C_IMPL)
            .addAnnotation(optInForeignApiAnnotation)
        for (function in functions) {
            fileBuilder.addFunction(singleFunction(function))
        }
        return fileBuilder.build()
    }

    /** @return success generated */
    fun generate(allImplFunc: Sequence<KSFunctionDeclaration>): Boolean {
        val functionsByPackage: Map<String, List<KSFunctionDeclaration>> =
            allImplFunc.groupBy { it.packageName.asString() }
        if (functionsByPackage.isEmpty()) return false
        functionsByPackage.forEach { (packageName, functions) ->
            generateFile(packageName, functions).writeTo(
                context.envContext.codeGenerator, Dependencies.ALL_FILES
            )
        }
        return true
    }
}

private const val C_IMPL = "_kni_gen"
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

private val optInForeignApiAnnotation = AnnotationSpec.builder(
    ClassName("kotlin", "OptIn")
).addMember(
    "%T::class",
    ClassName("kotlinx.cinterop", "ExperimentalForeignApi")
).build()
