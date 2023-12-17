package zsu.kni.ksp.native

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import zsu.kni.internal.JniTypeName
import zsu.kni.internal.JvmBytecodeType

class NativeNames(private val jniPackage: String) {
    private fun jni(name: String): ClassName = ClassName(jniPackage, name)
    private fun cinteropClass(name: String): ClassName = ClassName("kotlinx.cinterop", name)
    private fun cinteropMember(name: String): MemberName = MemberName("kotlinx.cinterop", name)

    val jniEnvVarName = jni("JNIEnvVar")
    val jObject = jni(JvmBytecodeType.L.jniName)
    val jClass = jni("jclass")

    val cPtr = cinteropClass("CPointer")
    val jenvPtr = cPtr.parameterizedBy(jniEnvVarName)

    val nativeBridge = ClassName("zsu.kni.internal.native", "NativeBridge")

    /**
     * ```
     * fun call(
     *     obj: O, methodName: String, methodDesc: MethodDesc,
     *     args: List<Pair<JvmBytecodeType, V>>, returnType: JvmBytecodeType,
     * ): V?
     * ```
     */
    val callMember = MemberName(nativeBridge, "call")

    /**
     * ```
     * fun callStatic(
     *     clazz: InternalName, methodName: String, methodDesc: MethodDesc,
     *     args: List<Pair<JvmBytecodeType, V>>, returnType: JvmBytecodeType,
     * ): V?
     * ```
     */
    val callStaticMember = MemberName(nativeBridge, "callStatic")

    /**
     * ```
     * inline fun <reified T> c2jType(
     *     cObject: T, jvmSerializerClass: InternalName, jvmSerializerMethod: String
     * ): O
     * ```
     */
    val c2j = "c2jType"

    /**
     * ```
     * inline fun <reified T> j2cType(
     *     jObj: O, jvmSerializerClass: InternalName, jvmGeneratorMethod: String,
     * ): T
     * ```
     */
    val j2c = "j2cType"

    /**
     * NativeBridge.objAsValue
     * ```
     * fun objAsValue(any: Any, jvmBytecodeType: JvmBytecodeType): V
     * ```
     */
    val objAsValue = "objAsValue"

    val memScoped = cinteropMember("memScoped")
    fun jni(jniTypeName: JniTypeName): ClassName = jni(jniTypeName.jniName)
}