package zsu.kni.ksp

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

class SerializerClassGenerator(
    private val context: KniContext,
) {
    fun createDeserializeFunction(className: ClassName): FunSpec {
    }

    fun createSerializeFunction(className: ClassName): FunSpec {
        val fqName = className.canonicalName.replace('.', '/')
        FunSpec.builder(fqName.mangled())
            .returns()
    }
}