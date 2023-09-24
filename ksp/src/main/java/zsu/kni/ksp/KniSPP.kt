package zsu.kni.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

class KniSPP : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return when (val platformInfo = environment.platforms.singleOrNull()) {
            is NativePlatformInfo -> KniNativeSP(environment)
            is JvmPlatformInfo -> KniJvmSP(environment)
            null -> KniCommonSP(environment)
            else -> error("unsupported platform: $platformInfo")
        }
    }
}

private class KniCommonSP(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        TODO("Not yet implemented")
    }
}

private class KniNativeSP(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        TODO("Not yet implemented")
    }
}

private class KniJvmSP(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        TODO("Not yet implemented")
    }
}