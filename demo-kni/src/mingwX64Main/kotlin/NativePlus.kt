import kotlinx.cinterop.*
import platform.posix.va_list
import zsu.jni.JNIEnvVar
import zsu.jni.jclass
import zsu.jni.jint
import zsu.jni.jvalue

@OptIn(ExperimentalForeignApi::class)
@CName("Java_native_NativePlusKt_nativePlus")
fun nativePlus(env: CPointer<JNIEnvVar>, clazz: jclass, a: jint, b: jint): jint {
    val a: jvalue? = null
    a!!.l
    env.pointed.pointed.CallStaticVoidMethod.(env, null, null, null)
    println(a)
    println(b)
    return a + b + 1 // plus 1, make you believe this is from native
}

