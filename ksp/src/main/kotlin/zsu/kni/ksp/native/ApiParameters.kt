package zsu.kni.ksp.native

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toClassName
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.actualParentClass
import zsu.kni.ksp.isStatic

class ApiParameters(
    val thisPart: ParameterSpec?,
    // paramName to typeName
    val params: List<ParameterSpec>,
) {
    companion object {
        fun from(context: KniContext, function: KSFunctionDeclaration): ApiParameters {
            val isStatic = function.isStatic()
            var thisPart: ParameterSpec? = null
            if (!isStatic) run {
                val actualParentClass = function.actualParentClass() ?: return@run
                thisPart = ParameterSpec("_this", actualParentClass.toClassName())
            }
            // params
            val parameters = function.parameters
            val params = ArrayList<ParameterSpec>(parameters.size)
            for (parameter in parameters) {
                val paramType = parameter.type.resolve()
                val paramTypeName = paramType.toClassName()
                params += ParameterSpec("p_${parameter.name!!.asString()}", paramTypeName)
            }
            return ApiParameters(thisPart, params)
        }
    }
}