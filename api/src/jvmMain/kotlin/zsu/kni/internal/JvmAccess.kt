package zsu.kni.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KType

/**
 * it will be called when access jvm from native
 */
@OptIn(ExperimentalSerializationApi::class)
object JvmAccess {
    fun serializeObject(obj: Any, kType: KType): ByteArray {
        val serializer = serializer(kType)
        return ProtoBuf.encodeToByteArray(serializer, obj)
    }

    fun deserializeObject(byteArray: ByteArray, kType: KType): Any? {
        val serializer = serializer(kType)
        return ProtoBuf.decodeFromByteArray(serializer, byteArray)
    }
}