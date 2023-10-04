package zsu.kni.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.ClassName
import zsu.kni.internal.InternalName
import zsu.kni.ksp.native.NativeNames

class KniEnvContext(env: SymbolProcessorEnvironment) : KSPLogger by env.logger {
    private val options = env.options
    val generatedPackage = env.options["kni-generated-package"] ?: "zsu.kni.generated"

    val generatedProtoName = env.options["kni-generated-proto-name"] ?: "JniNativeProto_generated"
    val protoClassName = ClassName(generatedPackage, generatedProtoName)

    val serializerName = env.options["kni-generated-serializer-name"] ?: "Serializer_generated"
    val serializerClassName = ClassName(generatedPackage, serializerName)
    val serializerInternalName: InternalName = serializerClassName.internalName


    // for android, normally "platform.android"
    val jniPackage = env.options["kni-jni-package"] ?: throw IllegalArgumentException(
        "must specify jni package(which contains jint, jstring...) through ksp argument: `kni-jni-package`. " +
                "For Android platform, usually `platform.android`."
    )

    val codeGenerator = env.codeGenerator
    val nativeNames = NativeNames(jniPackage)
}

class KniContext(
    val envContext: KniEnvContext,
    val resolver: Resolver
) : KSPLogger by envContext {
    val buildInTypes: KtBuildInTypes = KtBuildInTypes(resolver)
}