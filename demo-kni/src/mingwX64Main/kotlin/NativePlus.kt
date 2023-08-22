import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import zsu.jni.JNIEnvVar
import zsu.jni.jclass
import zsu.jni.jint

@OptIn(ExperimentalForeignApi::class)
@CName("Java_native_NativePlusKt_nativePlus")
fun nativePlus(env: CPointer<JNIEnvVar>, clazz: jclass, a: jint, b: jint): jint {
    println(a)
    println(b)
    return a + b + 1 // plus 1, make you believe this is from native
}