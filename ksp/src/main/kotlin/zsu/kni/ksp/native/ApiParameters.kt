package zsu.kni.ksp.native

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.actualParentClass
import zsu.kni.ksp.getJniName

typealias OriginName = String

data class ApiParamRecord(
    val originName: OriginName,
    val param: ParameterSpec,
) {
    val jvmBytecodeType: JvmBytecodeType = when (param.type) {
        BYTE -> JvmBytecodeType.B
        CHAR -> JvmBytecodeType.C
        DOUBLE -> JvmBytecodeType.D
        FLOAT -> JvmBytecodeType.F
        INT -> JvmBytecodeType.I
        LONG -> JvmBytecodeType.J
        SHORT -> JvmBytecodeType.S
        BOOLEAN -> JvmBytecodeType.Z
        else -> JvmBytecodeType.L
    }
}

class ApiParameters(
    val thisPart: ClassName,
    val params: List<ApiParamRecord>,
    val returnTypeName: TypeName,
    val returnBytecodeType: JvmBytecodeType,
) : ProcessingParameters {
    companion object {
        @OptIn(KspExperimental::class)
        fun from(context: KniContext, function: KSFunctionDeclaration): ApiParameters {
            var thisPart: ClassName? = null
            val actualParentClass = function.actualParentClass()
            if (actualParentClass != null) {
                thisPart = actualParentClass.toClassName()
            }
            if (thisPart == null) {
                val staticClassName = context.resolver.getOwnerJvmClassName(function)
                    ?: throw IllegalArgumentException(
                        "cannot process $function(qualified: ${function.qualifiedName})"
                    )
                thisPart = ClassName.bestGuess(staticClassName)
            }
            // params
            val parameters = function.parameters
            val params = ArrayList<ApiParamRecord>(parameters.size)
            for (parameter in parameters) {
                val paramName = requireNotNull(parameter.name).asString()
                val paramType = parameter.type.resolve()
                val paramTypeName = paramType.toTypeName()
                params += ApiParamRecord(paramName, ParameterSpec("p_$paramName", paramTypeName))
            }
            // return
            val returnType = function.returnType?.resolve()
            val returnTypeName: TypeName
            val returnBytecodeType: JvmBytecodeType
            if (returnType == null) {
                returnTypeName = UNIT
                returnBytecodeType = JvmBytecodeType.V
            } else {
                returnTypeName = returnType.toTypeName()
                returnBytecodeType = returnType.getJniName(context).toBytecodeType()
            }
            return ApiParameters(thisPart, params, returnTypeName, returnBytecodeType)
        }
    }
}