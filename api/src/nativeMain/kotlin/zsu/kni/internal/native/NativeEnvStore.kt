@file:OptIn(ExperimentalForeignApi::class)

package zsu.kni.internal.native

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

// mirror for jni
private typealias JNIEnvVar = CPointed
private typealias JEnvPtr = CPointer<out JNIEnvVar>

@OptIn(ExperimentalForeignApi::class)
object NativeEnvStore {
    private val envStoreMap by lazy {
        hashMapOf<Int, JEnvPtr>()
    }

    operator fun set(id: Int, env: JEnvPtr) {
        envStoreMap[id] = env
    }

    /** must ensure you will acquire a JNIEnv */
    operator fun <V : CPointed> get(id: Int): CPointer<V> {
        val jEnvPtr = envStoreMap[id] ?: throw IllegalStateException(
            "cannot found thread id $id when try to acquire jni environment. " +
                    "pls check if you have added this id in KniNativeThread(in jvm)."
        )
        @Suppress("UNCHECKED_CAST")
        return jEnvPtr as CPointer<V>
    }

    fun clear() {
        envStoreMap.clear()
    }

    fun remove(id: Int): JEnvPtr? {
        return envStoreMap.remove(id)
    }

    fun compareEnv(originPointed: JNIEnvVar, currentPointed: JNIEnvVar) {
        require(originPointed == currentPointed) {
            "jenv not same, origin: $originPointed, current: $currentPointed"
        }
    }
}