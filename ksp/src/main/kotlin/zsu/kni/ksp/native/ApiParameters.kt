package zsu.kni.ksp.native

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.actualParentClass

typealias OriginName = String
typealias ApiParamRecord = Pair<OriginName, ParameterSpec>

class ApiParameters(
    val thisPart: ParameterSpec,
    val params: List<ApiParamRecord>,
) : Parameters {
    companion object {
        @OptIn(KspExperimental::class)
        fun from(context: KniContext, function: KSFunctionDeclaration): ApiParameters {
            var thisPart: ParameterSpec? = null
            val actualParentClass = function.actualParentClass()
            if (actualParentClass != null) {
                thisPart = ParameterSpec("_this", actualParentClass.toClassName())
            }
            if (thisPart == null) {
                val staticClassName = context.resolver.getOwnerJvmClassName(function)
                    ?: throw IllegalArgumentException(
                        "cannot process $function(qualified: ${function.qualifiedName})"
                    )
                thisPart = ParameterSpec("_this", ClassName.bestGuess(staticClassName))
            }
            // params
            val parameters = function.parameters
            val params = ArrayList<ApiParamRecord>(parameters.size)
            for (parameter in parameters) {
                val paramName = requireNotNull(parameter.name).asString()
                val paramType = parameter.type.resolve()
                val paramTypeName = paramType.toTypeName()
                params += paramName to ParameterSpec("p_$paramName", paramTypeName)
            }
            return ApiParameters(thisPart, params)
        }
    }
}