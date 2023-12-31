@file:OptIn(ExperimentalForeignApi::class)

package zsu.kni.internal.native

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import zsu.kni.internal.InternalName
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.internal.MethodDesc
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * @param O : CPointer<*> jobject pointer
 * @param V : CVariable jvalue
 * @param M : CPointer<*> jmethodId
 */
@OptIn(ExperimentalForeignApi::class)
class NativeBridge<O : CPointer<*>, V : CVariable, M : CPointer<*>>(
    proto: NativeProto<O, V, M>,
) : NativeProto<O, V, M> by proto {
    /**
     * @see [NativeProto.callStatic]
     */
    fun callStatic(
        clazz: InternalName, methodName: String, methodDesc: MethodDesc,
        args: List<Pair<JvmBytecodeType, V>>, returnType: JvmBytecodeType,
    ): V? {
        val jclass = getJClass(clazz)
        val methodId = getMethodId(jclass, true, methodName, methodDesc)
        return callStatic(jclass, methodId, args.toArrayPtr(), returnType)
    }

    /**
     * @see [NativeProto.call]
     */
    fun call(
        obj: O, methodName: String, methodDesc: MethodDesc,
        args: List<Pair<JvmBytecodeType, V>>, returnType: JvmBytecodeType,
    ): V? {
        val objClass = getObjClass(obj)
        val methodId = getMethodId(objClass, false, methodName, methodDesc)
        return call(obj, methodId, args.toArrayPtr(), returnType)
    }

    /** wrap j-xxx(such as jobject/jint) -> jvalue */
    fun objAsValue(any: Any, jvmBytecodeType: JvmBytecodeType): V {
        @Suppress("UNCHECKED_CAST")
        return if (jvmBytecodeType == JvmBytecodeType.L) {
            (any as O).obtainV // jobject
        } else any.anyAsV // other types
    }

    /**
     * @param jObj java object
     * @return native object
     */
    inline fun <reified T> j2cType(
        jObj: O, jvmSerializerClass: InternalName, jvmSerializerMethod: String
    ): T {
        return j2cType(jObj, jvmSerializerClass, jvmSerializerMethod, typeOf<T>())
    }

    @PublishedApi
    @OptIn(ExperimentalSerializationApi::class)
    internal fun <T> j2cType(
        jObj: O, jvmSerializerClass: InternalName, jvmGeneratorMethod: String, type: KType
    ): T {
        val jArrayValue = callStatic(
            jvmSerializerClass, jvmGeneratorMethod, S_GENERATOR_DESC,
            listOf(JvmBytecodeType.L to jObj.obtainV),
            JvmBytecodeType.L
        )!!
        val jArrayObj = jArrayValue.obtainO
        val (valuesPointer, length) = getBytes(jArrayObj)
        try {
            val bytes = valuesPointer.readBytes(length)

            @Suppress("UNCHECKED_CAST") // unsafe, but safe I think
            val obj = ProtoBuf.decodeFromByteArray(serializer(type), bytes) as T
            return obj
        } finally {
            releaseBytes(jArrayObj, valuesPointer, false)
        }
    }

    /**
     * @param cObject native object
     * @return jobject
     */
    inline fun <reified T> c2jType(
        cObject: T, jvmSerializerClass: InternalName, jvmSerializerMethod: String
    ): O {
        return c2jType(cObject, jvmSerializerClass, jvmSerializerMethod, typeOf<T>())
    }

    @PublishedApi
    @OptIn(ExperimentalSerializationApi::class)
    internal fun <T> c2jType(
        cObject: T, jvmSerializerClass: InternalName, jvmSerializerMethod: String, type: KType,
    ): O {
        val bytes = ProtoBuf.encodeToByteArray(serializer(type), cObject)
        val jBytes = createJBytes(bytes)
        val args = listOf(JvmBytecodeType.L to jBytes)
        return callStatic(
            jvmSerializerClass, jvmSerializerMethod, DS_GENERATOR_DESC,
            args, JvmBytecodeType.L
        )!!.obtainO // non-null, because cObject must not be a void
    }
}

private const val DS_GENERATOR_DESC: MethodDesc = "([B)Ljava/lang/Object;"
private const val S_GENERATOR_DESC: MethodDesc = "(Ljava/lang/Object;)[B"

