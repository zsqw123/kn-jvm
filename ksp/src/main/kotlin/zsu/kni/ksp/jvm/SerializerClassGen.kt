package zsu.kni.ksp.jvm

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.jvmStatic
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import zsu.kni.internal.jvm.JvmAccess
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.isCustomSerializerNeeded
import zsu.kni.ksp.serializerName

class SerializerClassGen(
    private val context: KniContext,
) {
    private val env = context.envContext

    private val jvmAccessClassName = JvmAccess::class.asClassName()
    private val jvmSerializeMemberName = MemberName(jvmAccessClassName, JvmAccess::serializeObject.name)
    private val jvmDeserializeMemberName = MemberName(jvmAccessClassName, JvmAccess::deserializeObject.name)

    private val serializedNames = hashMapOf<String, FunSpec>()
    private val deserializedNames = hashMapOf<String, FunSpec>()

    private fun getSerializeFun(className: TypeName): FunSpec {
        val serializerName = className.serializerName
        serializedNames[serializerName]?.let { return it }
        val funBuilder = FunSpec.builder(serializerName).jvmStatic()
            .addParameter("obj", Any::class)
            .addStatement("return·%M(obj, %M<%T>())", jvmSerializeMemberName, typeOfMember, className)
            .returns(ByteArray::class)
        return funBuilder.build().also { serializedNames[serializerName] = it }
    }

    private fun getDeserializeFun(className: TypeName): FunSpec {
        val serializerName = className.serializerName
        deserializedNames[serializerName]?.let { return it }
        val funBuilder = FunSpec.builder(serializerName).jvmStatic()
            .addParameter("array", ByteArray::class)
            .addStatement("return·%M(array, %M<%T>())", jvmDeserializeMemberName, typeOfMember, className)
            .returns(Any::class)
        return funBuilder.build().also { deserializedNames[serializerName] = it }
    }

    private fun getTypeSpec(): TypeSpec {
        return TypeSpec.objectBuilder(env.serializerClassName)
            .addFunctions(serializedNames.values)
            .addFunctions(deserializedNames.values)
            .build()
    }

    private fun generateAllCustomTypes(allAnnotatedFunc: List<KSFunctionDeclaration>) {
        val allTypes: Sequence<KSType> = sequence {
            allAnnotatedFunc.forEach {
                val parentClass = it.parentDeclaration as KSClassDeclaration?
                if (parentClass != null) {
                    yield(parentClass.asStarProjectedType())
                }
                yield(it.returnType!!.resolve())
                yieldAll(it.parameters.map { param -> param.type.resolve() })
            }
        }
        val allClassNames = allTypes
            .filter { it.isCustomSerializerNeeded(context) }
            .map { it.toTypeName() }
        allClassNames.forEach {
            getSerializeFun(it)
            getDeserializeFun(it)
        }
    }

    fun generate(allAnnotatedFunc: List<KSFunctionDeclaration>) {
        generateAllCustomTypes(allAnnotatedFunc)
        val typeSpec = getTypeSpec()
        val fileSpec = FileSpec.builder(env.serializerClassName)
            .addType(typeSpec).build()
        fileSpec.writeTo(env.codeGenerator, Dependencies.ALL_FILES)
    }
}

private val typeOfMember = MemberName("kotlin.reflect", "typeOf")
