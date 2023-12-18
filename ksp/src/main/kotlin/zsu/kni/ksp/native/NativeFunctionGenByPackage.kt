package zsu.kni.ksp.native

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.writeTo
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.optInExpNativeApiAnnotation

abstract class NativeFunctionGenByPackage<P : ProcessingParameters>(
    private val context: KniContext
) {
    protected val env = context.envContext
    protected val nativeNames = env.nativeNames

    abstract val generatedFileName: String

    abstract fun parametersFromFunction(function: KSFunctionDeclaration): P

    // STEP0: prepare a builder
    abstract fun prepareBuilder(function: KSFunctionDeclaration, parameters: P): FunSpec.Builder

    // STEP1: build proto & bridge
    abstract fun FunSpec.Builder.buildNativeBridge(function: KSFunctionDeclaration, parameters: P)

    // STEP2: build params
    abstract fun FunSpec.Builder.buildAllParams(function: KSFunctionDeclaration, parameters: P)

    // STEP3: build interop method function call
    abstract fun FunSpec.Builder.buildMethodCall(function: KSFunctionDeclaration, parameters: P)

    // STEP4: build return method
    abstract fun FunSpec.Builder.buildReturn(function: KSFunctionDeclaration, parameters: P)


    /**
     * @param function function declared in source code
     * @return generated glue code for declared source function
     */
    private fun buildFunction(function: KSFunctionDeclaration): FunSpec {
        val parameters = parametersFromFunction(function)
        return prepareBuilder(function, parameters).memScoped {
            buildNativeBridge(function, parameters)
            buildAllParams(function, parameters)
            buildMethodCall(function, parameters)
            buildReturn(function, parameters)
        }.build()
    }

    private fun generateFile(filePackage: String, functions: List<KSFunctionDeclaration>): FileSpec {
        val fileBuilder = FileSpec.builder(filePackage, generatedFileName)
            .addImport(jvmBytecodeType.packageName, jvmBytecodeType.simpleName)
            .addAnnotation(optInExpNativeApiAnnotation)
        for (function in functions) {
            fileBuilder.addFunction(buildFunction(function))
        }
        return fileBuilder.build()
    }

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

    protected inline fun FunSpec.Builder.memScoped(
        content: FunSpec.Builder.() -> Unit
    ): FunSpec.Builder = apply {
        beginControlFlow("%M", nativeNames.memScoped)
        content()
        endControlFlow()
    }
}

private val jvmBytecodeType = JvmBytecodeType::class.asClassName()
