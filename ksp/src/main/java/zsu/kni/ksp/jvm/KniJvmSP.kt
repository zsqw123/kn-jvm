package zsu.kni.ksp.jvm

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.KniEnvContext
import zsu.kni.ksp.jniApiFqn

class KniJvmSP(private val env: KniEnvContext) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val context = KniContext(env, resolver)
        val allJniApiFunc = resolver
            .getSymbolsWithAnnotation(jniApiFqn)
            .map { it as KSFunctionDeclaration }.toList()
        SerializerClassGen(context).generate(allJniApiFunc)
        return emptyList()
    }
}