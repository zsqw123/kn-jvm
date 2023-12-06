package zsu.kni.ksp.native

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.actualParentClass
import zsu.kni.ksp.getJniName
import zsu.kni.ksp.isStatic

class ApiParameters(
    val containingClassName: ClassName,
    val thisPart: ParameterSpec?,
    // paramName to typeName
    val params: List<ParameterSpec>,
) {
    companion object {
        @OptIn(KspExperimental::class)
        fun from(context: KniContext, function: KSFunctionDeclaration): ApiParameters {
            val isStatic = function.isStatic()
            var thisPart: ParameterSpec? = null
            val actualParentClass = function.actualParentClass()
            if (actualParentClass != null) {
                thisPart = ParameterSpec("_this", actualParentClass.toClassName())
            }
            if (thisPart == null) {
                val isTopLevel = function.parentDeclaration == null
                context.resolver.getOwnerJvmClassName(function)
                if (!isTopLevel) throw IllegalArgumentException(
                    "cannot process $function(qualified: ${function.qualifiedName})"
                )
                val ktFileClassName = function.containingFile?
            }
            // params
            val parameters = function.parameters
            val params = ArrayList<ParameterSpec>(parameters.size)
            for (parameter in parameters) {
                val paramType = parameter.type.resolve()
                val paramTypeName = paramType.toTypeName()
                params += ParameterSpec("p_${parameter.name!!.asString()}", paramTypeName)
            }
            return ApiParameters(thisPart, params)
        }
    }
}