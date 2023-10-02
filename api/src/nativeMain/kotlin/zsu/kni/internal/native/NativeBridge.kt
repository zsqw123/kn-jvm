@file:OptIn(ExperimentalForeignApi::class)

package zsu.kni.internal.native

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import zsu.kni.internal.BytecodeName
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.internal.MethodDesc
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@OptIn(ExperimentalForeignApi::class)
class NativeBridge<O : CPointer<*>, V : CVariable, M : CPointer<*>>(
    proto: NativeProto<O, V, M>,
) : NativeProto<O, V, M> by proto {
    /**
     * @return returns null means no return value: [Unit]
     */
    fun callStatic(
        clazz: BytecodeName, methodName: String, methodDesc: MethodDesc,
        args: List<Pair<JvmBytecodeType, V>>, returnType: JvmBytecodeType,
    ): V? {
        val jclass = getJClass(clazz)
        val methodId = getMethodId(jclass, true, methodName, methodDesc)
        return callStatic(jclass, methodId, args.toArrayPtr(), returnType)
    }

    /**
     * @return returns null means no return value: [Unit]
     */
    fun call(
        obj: O, methodName: String, methodDesc: MethodDesc,
        args: List<Pair<JvmBytecodeType, V>>, returnType: JvmBytecodeType,
    ): V? {
        val objClass = getObjClass(obj)
        val methodId = getMethodId(objClass, false, methodName, methodDesc)
        return call(obj, methodId, args.toArrayPtr(), returnType)
    }

    /**
     * @param jByteArrayInput jByteArray
     * @return native object
     */
    inline fun <reified T> j2cType(jByteArrayInput: O): T {
        return j2cType(jByteArrayInput, typeOf<T>())
    }

    @PublishedApi
    @OptIn(ExperimentalSerializationApi::class)
    internal fun <T> j2cType(
        jByteArrayInput: O, type: KType,
    ): T {
        val (valuesPointer, length) = getBytes(jByteArrayInput)
        try {
            val bytes = valuesPointer.readBytes(length)

            @Suppress("UNCHECKED_CAST") // unsafe, but safe I think
            val obj = ProtoBuf.decodeFromByteArray(serializer(type), bytes) as T
            return obj
        } finally {
            releaseBytes(jByteArrayInput, valuesPointer, false)
        }
    }

    /**
     * @param cObject native object
     * @return jobject
     */
    inline fun <reified T> c2jType(
        cObject: T, jvmGeneratorClass: BytecodeName, jvmGeneratorMethod: String,
    ): V {
        return c2jType(cObject, typeOf<T>(), jvmGeneratorClass, jvmGeneratorMethod)
    }

    @PublishedApi
    @OptIn(ExperimentalSerializationApi::class)
    internal fun <T> c2jType(
        cObject: T, type: KType, jvmGeneratorClass: BytecodeName, jvmGeneratorMethod: String,
    ): V {
        val bytes = ProtoBuf.encodeToByteArray(serializer(type), cObject)
        val jBytes = createJBytes(bytes)
        val args = listOf(JvmBytecodeType.L to jBytes)
        return callStatic(
            jvmGeneratorClass, jvmGeneratorMethod, GENERATOR_DESC,
            args, JvmBytecodeType.L
        )!! // non-null, because cObject must not be a void
    }
}

private const val GENERATOR_DESC: MethodDesc = "([B)Ljava/lang/Object;"

