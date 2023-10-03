package zsu.kni.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import zsu.kni.ksp.template.getNativeProtoImpl

class KniSPP : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val kniEnvContext = KniEnvContext(environment)
        return when (val platformInfo = environment.platforms.singleOrNull()) {
            is NativePlatformInfo -> KniNativeSP(kniEnvContext)
            is JvmPlatformInfo -> KniJvmSP(kniEnvContext)
            null -> KniCommonSP(kniEnvContext)
            else -> error("unsupported platform: $platformInfo")
        }
    }
}

private class KniCommonSP(private val context: KniEnvContext) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        return emptyList()
    }
}

private class KniNativeSP(private val context: KniEnvContext) : SymbolProcessor {
    private val nativeProtoDependencies = Dependencies(false) // shouldn't change

    override fun process(resolver: Resolver): List<KSAnnotated> {
        createNativeProtoFile()

    }

    private fun createNativeProtoFile() {
        val protoFile = context.codeGenerator.createNewFile(
            nativeProtoDependencies, context.generatedPackage, context.generatedProtoName
        )
        protoFile.bufferedWriter().use {
            val implText = getNativeProtoImpl(
                context.generatedPackage, context.generatedProtoName, context.jniPackage
            )
            it.write(implText)
        }
    }
}

private class KniJvmSP(private val context: KniEnvContext) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {

    }

    fun createSerializers() {

    }
}

