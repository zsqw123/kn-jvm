package zsu.kni.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import zsu.kni.ksp.jvm.KniJvmSP
import zsu.kni.ksp.native.KniNativeSP

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
