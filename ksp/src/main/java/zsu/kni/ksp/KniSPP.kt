package zsu.kni.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

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
        TODO("Not yet implemented")
    }
}

private class KniNativeSP(private val context: KniEnvContext) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        TODO("Not yet implemented")
    }
}

private class KniJvmSP(private val context: KniEnvContext) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        TODO("Not yet implemented")
    }
}

