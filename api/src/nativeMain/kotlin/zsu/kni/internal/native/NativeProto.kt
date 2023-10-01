@file:OptIn(ExperimentalForeignApi::class)

package zsu.kni.internal.native

import kotlinx.cinterop.*
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
     * @return returns null means no return value: [Unit]
     */
    fun callStatic(
        jClass: O, methodId: M,
        values: CArrayPointer<V>,
        returnType: JvmBytecodeType,
    ): V?

    /**
     * @return returns null means no return value: [Unit]
     */
    fun call(
        jObject: O, methodId: M,
        values: CArrayPointer<V>,
        returnType: JvmBytecodeType,
    ): V?

    fun getMethodId(
        clazzName: BytecodeName, isStatic: Boolean,
        methodName: String, methodDesc: MethodDesc
    ): M

    fun getBytes(jByteArray: O): CArrayPointer<ByteVar>

    fun releaseBytes(jByteArray: O, valuesPointer: CArrayPointer<ByteVar>)
}
