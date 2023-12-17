package zsu.kni.internal

import kotlin.jvm.JvmInline

// eg: jint/jboolean...
@JvmInline
value class JniTypeName(val jniName: String) {
    fun toBytecodeType(): JvmBytecodeType = JvmBytecodeType.valueOf(jniName)
    companion object {
        val VOID = JniTypeName("void")
    }
}

@Suppress("SpellCheckingInspection")
enum class JvmBytecodeType(val jniName: String) {
    B("jbyte"),
    C("jchar"),
    D("jdouble"),
    F("jfloat"),
    I("jint"),
    J("jlong"),
    S("jshort"),
    Z("jboolean"),
    L("jobject"),
    V(JniTypeName.VOID.jniName), // void is special
}

// java/lang/Object
typealias InternalName = String
typealias MethodDesc = String
