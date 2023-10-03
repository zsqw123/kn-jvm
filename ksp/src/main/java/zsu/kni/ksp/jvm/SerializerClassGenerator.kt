package zsu.kni.ksp.jvm

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.jvmStatic
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import zsu.kni.internal.jvm.JvmAccess
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.mangled

class SerializerClassGenerator(
    context: KniContext,
) {
    private val env = context.envContext

    private val jvmAccessClassName = JvmAccess::class.asClassName()
    private val jvmSerializeMemberName = MemberName(jvmAccessClassName, JvmAccess::serializeObject.name)
    private val jvmDeserializeMemberName = MemberName(jvmAccessClassName, JvmAccess::deserializeObject.name)

    private val serializedNames = hashMapOf<String, FunSpec>()
    private val deserializedNames = hashMapOf<String, FunSpec>()

    private fun getDeserializeFun(className: ClassName): FunSpec {
        val fqName = className.canonicalName
        deserializedNames[fqName]?.let { return it }
        val funBuilder = FunSpec.builder(fqName.replace('.', '/').mangled()).jvmStatic()
            .addParameter("obj", Any::class)
            .addStatement("return·%M(obj, typeOf<%T>())", jvmDeserializeMemberName, className)
            .returns(ByteArray::class)
        return funBuilder.build().also { deserializedNames[fqName] = it }
    }

    private fun getSerializeFun(className: ClassName): FunSpec {
        val fqName = className.canonicalName
        serializedNames[fqName]?.let { return it }
        val funBuilder = FunSpec.builder(fqName.replace('.', '/').mangled()).jvmStatic()
            .addParameter("array", ByteArray::class)
            .addStatement("return·%M(array, typeOf<%T>())", jvmSerializeMemberName, className)
            .returns(Any::class)
        return funBuilder.build().also { serializedNames[fqName] = it }
    }

    private val generatedClassName = ClassName(env.generatedPackage, env.generatedProtoName)
    private fun getTypeSpec(): TypeSpec {
        return TypeSpec.objectBuilder(generatedClassName)
            .addFunctions(serializedNames.values)
            .addFunctions(deserializedNames.values)
            .build()
    }

    private fun generateAllCustomTypes(allAnnotatedFunc: List<KSFunctionDeclaration>) {
        val allTypes = ArrayList<KSType>().apply {
            allAnnotatedFunc.forEach {
                val parentClass = it.parentDeclaration as KSClassDeclaration?
                if (parentClass != null) {
                    add(parentClass.asStarProjectedType())
                }
                add(it.returnType!!.resolve())
                addAll(it.parameters.map { param -> param.type.resolve() })
            }
        }
        val allClassNames = allTypes.map { it.toClassName() }
        allClassNames.forEach {
            getSerializeFun(it)
            getDeserializeFun(it)
        }
    }

    fun generate(allAnnotatedFunc: List<KSFunctionDeclaration>) {
        generateAllCustomTypes(allAnnotatedFunc)
        val typeSpec = getTypeSpec()
        val fileSpec = FileSpec.builder(generatedClassName)
            .addType(typeSpec).build()
        fileSpec.writeTo(env.codeGenerator, Dependencies.ALL_FILES)
    }
}