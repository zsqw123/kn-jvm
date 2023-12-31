package zsu.kni.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import zsu.kni.internal.InternalName
import zsu.kni.internal.MethodDesc
import zsu.kni.ksp.native.NativeNames

class KniEnvContext(env: SymbolProcessorEnvironment) : KSPLogger by env.logger {
    private val options = env.options
    val generatedPackage = options["kni-generated-package"] ?: "zsu.kni.generated"

    val protoName = options["kni-generated-proto-name"] ?: "JniNativeProto"
    val protoClassName = ClassName(generatedPackage, protoName)

    val envStoreClassName = ClassName("zsu.kni.internal.native", "NativeEnvStore")
    val envGetMember = MemberName(envStoreClassName,"get")

    val jniEnvStoreName = options["kni-generated-env-store-name"] ?: "JniNativeEnvStore"
    val jniEnvStoreClassName = ClassName(generatedPackage, jniEnvStoreName)

    val serializerName = options["kni-generated-serializer-name"] ?: "Serializer"
    val serializerClassName = ClassName(generatedPackage, serializerName)
    val serializerInternalName: InternalName = serializerClassName.internalName

    // for android, normally "platform.android"
    val jniPackage = options["kni-jni-package"] ?: throw IllegalArgumentException(
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

    // get class from poet ClassName
    fun optClass(className: ClassName): KSClassDeclaration? {
        return resolver.getClassDeclarationByName(className.canonicalName)
    }

    @OptIn(KspExperimental::class)
    fun optMethodDesc(function: KSFunctionDeclaration): MethodDesc? {
        return resolver.mapToJvmSignature(function)
    }
}
