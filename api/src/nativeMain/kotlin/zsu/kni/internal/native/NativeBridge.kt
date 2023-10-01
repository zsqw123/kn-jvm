@file:OptIn(ExperimentalForeignApi::class)

package zsu.kni.internal.native

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.ExperimentalForeignApi
import zsu.kni.internal.BytecodeName
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.internal.MethodDesc

@OptIn(ExperimentalForeignApi::class)
class NativeBridge<O : CPointer<*>, V : CVariable, M : CPointer<*>>(
    proto: NativeProto<O, V, M>,
) : NativeProto<O, V, M> by proto {
    fun callStatic(
        clazz: BytecodeName, methodName: String, methodDesc: MethodDesc,
        args: List<Pair<JvmBytecodeType, V>>, returnType: JvmBytecodeType,
    ): V? {
        val jclass = getJClass(clazz)
        val methodId = getMethodId(jclass, true, methodName, methodDesc)
        return callStatic(jclass, methodId, args.toArrayPtr(), returnType)
    }

    fun call(
        obj: O, methodName: String, methodDesc: MethodDesc,
        args: List<Pair<JvmBytecodeType, V>>, returnType: JvmBytecodeType,
    ): V? {
        val objClass = getObjClass(obj)
        val methodId = getMethodId(objClass, false, methodName, methodDesc)
        return call(obj, methodId, args.toArrayPtr(), returnType)
    }
}
