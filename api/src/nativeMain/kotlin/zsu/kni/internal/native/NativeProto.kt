@file:OptIn(ExperimentalForeignApi::class)

package zsu.kni.internal.native

import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import zsu.kni.internal.BytecodeName
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.internal.MethodDesc

/**
 * most basic JNI implementation protocol, implemented in each platform
 * @param O type of jobject/jclass
 * @param V type of jvalue
 * @param M type of jmethodId
 */
interface NativeProto<O : CPointer<*>, V : CPointed, M : CPointer<*>> {
    /**
     * @return returns null means no return value, such as: [Unit]
     */
    fun callStatic(
        jClass: O, methodId: M,
        values: CArrayPointer<V>,
        type: JvmBytecodeType,
    ): V?

    fun call(
        jObject: O, methodId: M,
        values: CArrayPointer<V>,
        type: JvmBytecodeType,
    ): V?

    fun getMethodId(
        clazzName: BytecodeName, isStatic: Boolean,
        methodName: String, methodDesc: MethodDesc
    ): M
}