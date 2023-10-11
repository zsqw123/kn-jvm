package zsu.kni.ksp.native

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.KniEnvContext
import zsu.kni.ksp.jniApiFqn
import zsu.kni.ksp.jniImplFqn
import zsu.kni.ksp.template.getNativeProtoImpl

class KniNativeSP(private val env: KniEnvContext) : SymbolProcessor {
    private val nativeProtoDependencies = Dependencies(false) // shouldn't change

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

        if (hasGeneratedImpl || hasGeneratedApi) createNativeProtoFile(context)

        return emptyList()
    }

    private fun createNativeProtoFile(context: KniContext) {
        val existedProto = context.optClass(env.protoClassName)
        if (existedProto != null) {
            // for android platform, we have pre-built proto class.
            return
        }
        val protoFile = env.codeGenerator.createNewFile(
            nativeProtoDependencies, env.generatedPackage, env.generatedProtoName
        )
        protoFile.bufferedWriter().use {
            val implText = getNativeProtoImpl(
                env.generatedPackage, env.generatedProtoName, env.jniPackage
            )
            it.write(implText)
        }
    }

    private fun KniContext.containsNativeProto() {

    }

    private fun KniContext.contains
}
