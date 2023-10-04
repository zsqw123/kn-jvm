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

    val jniEnvName = jni("JNIEnvVar")
    val jObject = jni(JvmBytecodeType.L.jniName)
    val jClass = jni("jclass")

    val cPtr = cinteropClass("CPointer")
    val jenvPtr = cPtr.parameterizedBy(jniEnvName)

    val nativeBridge = ClassName("zsu.kni.internal.native", "NativeBridge")

    val memScoped = cinteropMember("memScoped")
    fun jni(jniTypeName: JniTypeName): ClassName = jni(jniTypeName.jniName)
}