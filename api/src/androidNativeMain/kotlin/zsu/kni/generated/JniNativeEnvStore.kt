package zsu.kni.generated

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import platform.android.JNIEnvVar
import platform.android.jclass
import platform.android.jint
import zsu.kni.internal.native.NativeEnvStore
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
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

