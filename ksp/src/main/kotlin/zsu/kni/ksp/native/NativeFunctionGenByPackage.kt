package zsu.kni.ksp.native

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.writeTo
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.optInForeignApiAnnotation

abstract class NativeFunctionGenByPackage(
    private val context: KniContext
) {
    abstract val generatedFileName: String

    abstract fun singleFunction(function: KSFunctionDeclaration): FunSpec

    private fun generateFile(filePackage: String, functions: List<KSFunctionDeclaration>): FileSpec {
        val fileBuilder = FileSpec.builder(filePackage, generatedFileName)
            .addAnnotation(optInForeignApiAnnotation)
        for (function in functions) {
            fileBuilder.addFunction(singleFunction(function))
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
}