package zsu.kni.ksp.native

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.KniEnvContext
import zsu.kni.ksp.jniApiFqn
import zsu.kni.ksp.jniImplFqn
import zsu.kni.ksp.template.NativeEnvStoreImpl
import zsu.kni.ksp.template.NativeProtoImpl
import zsu.kni.ksp.template.Template

class KniNativeSP(private val env: KniEnvContext) : SymbolProcessor {
    private val nativeStableGeneratedDependencies = Dependencies(false) // never change

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val context = KniContext(env, resolver)
        val allJniImplFunc: Sequence<KSFunctionDeclaration> = resolver
            .getSymbolsWithAnnotation(jniImplFqn)
            .map { it as KSFunctionDeclaration }
        val hasGeneratedImpl = NativeImplGen(context).generate(allJniImplFunc)

        val allJniApiFunc: Sequence<KSFunctionDeclaration> = resolver
            .getSymbolsWithAnnotation(jniApiFqn)
            .map { it as KSFunctionDeclaration }
        val hasGeneratedApi = NativeApiGen(context).generate(allJniApiFunc)

        if (hasGeneratedImpl || hasGeneratedApi) {
            context.createNativeTemplateFile(NativeEnvStoreImpl, env.envStoreClassName)
            context.createNativeTemplateFile(NativeProtoImpl, env.protoClassName)
        }

        return emptyList()
    }

    private fun KniContext.createNativeTemplateFile(
        template: Template, generatedClassName: ClassName,
    ) {
        if (optClass(generatedClassName) != null) {
            // for some platforms(like android), we have pre-built template class.
            return
        }
        val simpleClassName = generatedClassName.simpleName
        val protoFile = env.codeGenerator.createNewFile(
            nativeStableGeneratedDependencies, env.generatedPackage, simpleClassName
        )
        protoFile.bufferedWriter().use {
            val implText = template.create(
                generatedClassName.packageName, simpleClassName, env.jniPackage,
            )
            it.write(implText)
        }
    }
}
