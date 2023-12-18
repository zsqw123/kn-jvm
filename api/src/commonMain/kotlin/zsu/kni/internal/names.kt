package zsu.kni.internal

import kotlin.jvm.JvmInline

// eg: jint/jboolean...
@JvmInline
value class JniTypeName(val jniName: String) {
    fun toBytecodeType(): JvmBytecodeType = JvmBytecodeType.jniNameOf(jniName)
    companion object {
        val VOID = JniTypeName("void")
    }
}

@Suppress("SpellCheckingInspection")
enum class JvmBytecodeType(val jniName: String, val jvaluePropName: String) {
    B("jbyte", "b"),
    C("jchar", "c"),
    D("jdouble", "d"),
    F("jfloat", "f"),
    I("jint", "i"),
    J("jlong", "j"),
    S("jshort", "s"),
    Z("jboolean", "z"),
    L("jobject", "l"),
    V(JniTypeName.VOID.jniName, "l"), // void is special;
    ;

    companion object {
        private val values by lazy { JvmBytecodeType.entries }
        fun jniNameOf(jniName: String): JvmBytecodeType {
            return values.first { it.jniName == jniName }
        }
    }
}

// java/lang/Object
typealias InternalName = String
typealias MethodDesc = String
