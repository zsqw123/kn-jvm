package zsu.kni.ksp.template

import org.intellij.lang.annotations.Language

object NativeEnvStoreImpl : Template {
    @Language("kotlin")
    override fun create(packageName: String, simpleClassName: String, jniPackageName: String): String {
        return """
package $packageName

import kotlinx.cinterop.*
import $jniPackageName.*
import zsu.kni.internal.native.NativeEnvStore

@OptIn(ExperimentalForeignApi::class)
object JniNativeEnvStore {
    @CName(externName = "Java_zsu_kni_KniNativeThread_attach")
    fun attach(
        jenv: CPointer<JNIEnvVar>,
        classRef: jclass,
        id: jint,
    ) {
        NativeEnvStore[id] = jenv
    }

    @CName(externName = "Java_zsu_kni_KniNativeThread_detach")
    fun detach(
        jenv: CPointer<JNIEnvVar>,
        classRef: jclass,
        id: jint,
        sameJEnv: jint,
    ) {
        val isSameJEnv = sameJEnv == 0
        if (isSameJEnv) {
            val originJenvPtr: CPointer<JNIEnvVar> = NativeEnvStore[id]
            val originJenvValue = originJenvPtr.pointed
            val currentJenvValue = jenv.pointed
            NativeEnvStore.compareEnv(originJenvValue, currentJenvValue)
        }
        NativeEnvStore.remove(id)
    }

    @CName(externName = "Java_zsu_kni_KniNativeThread_detachAll")
    fun detachAll(
        jenv: CPointer<JNIEnvVar>,
        classRef: jclass,
    ) {
        NativeEnvStore.clear()
    }
}
        """
    }
}