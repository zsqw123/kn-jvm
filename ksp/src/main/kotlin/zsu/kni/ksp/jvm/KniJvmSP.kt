package zsu.kni.ksp.jvm

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import zsu.kni.ksp.KniContext
import zsu.kni.ksp.KniEnvContext
import zsu.kni.ksp.jniApiFqn
import zsu.kni.ksp.jniImplFqn

class KniJvmSP(private val env: KniEnvContext) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val context = KniContext(env, resolver)
        val allNeededFunction = sequence {
            yieldAll(resolver.getSymbolsWithAnnotation(jniApiFqn))
            yieldAll(resolver.getSymbolsWithAnnotation(jniImplFqn))
        }.filterIsInstance<KSFunctionDeclaration>().toList()
        if (allNeededFunction.isEmpty()) return emptyList()
        SerializerClassGen(context).generate(allNeededFunction)
        return emptyList()
    }
}