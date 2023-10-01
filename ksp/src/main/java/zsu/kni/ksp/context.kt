package zsu.kni.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

class KniEnvContext(env: SymbolProcessorEnvironment) : KSPLogger by env.logger {
    private val options = env.options
    val generatedPackage = env.options["kni-generated-package"] ?: "zsu.kni.generated"

    // for android, normally "platform.android"
    val jniPackage = env.options["kni-jni-package"] ?: throw IllegalArgumentException(
        "must specify jni package(which contains jint, jstring...) through ksp argument: `kni-jni-package`. " +
                "For Android platform, usually `platform.android`."
    )

    val codeGenerator = env.codeGenerator
}

class KniContext(
    val envContext: KniEnvContext,
    val resolver: Resolver
) : KSPLogger by envContext {
    val buildInTypes: KtBuildInTypes = KtBuildInTypes(resolver)

}