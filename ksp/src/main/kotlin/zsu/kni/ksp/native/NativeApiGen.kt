package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import zsu.kni.ksp.KniContext

class NativeApiGen(
    private val context: KniContext
) : NativeFunctionGenByPackage(context) {
    override val generatedFileName: String = C_API
    override fun singleFunction(function: KSFunctionDeclaration): FunSpec {
        TODO("Not yet implemented")
    }
}

private const val C_API = "_kni_api"
