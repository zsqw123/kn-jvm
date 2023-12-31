package zsu.kni.generated

import kotlinx.cinterop.*
import platform.android.*
import zsu.kni.internal.InternalName
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.internal.MethodDesc
import zsu.kni.internal.native.NativeProto

@Suppress("unused") // used for android native side stub impl
@OptIn(ExperimentalForeignApi::class)
class JniNativeProto(
    private val envPtr: CPointer<JNIEnvVar>,
    private val memAllocator: NativePlacement,
) : NativeProto<jobject, jvalue, jmethodID> {
    private val jEnv = envPtr.pointed.pointed!!
    private val findClassPtr = jEnv.FindClass!!
    private val findMethodPtr = jEnv.GetMethodID!!
    private val findStaticMethodPtr = jEnv.GetStaticMethodID!!

    private val callBoolean = jEnv.CallBooleanMethodA!!
    private val callChar = jEnv.CallCharMethodA!!
    private val callByte = jEnv.CallByteMethodA!!
    private val callShort = jEnv.CallShortMethodA!!
    private val callInt = jEnv.CallIntMethodA!!
    private val callLong = jEnv.CallLongMethodA!!
    private val callFloat = jEnv.CallFloatMethodA!!
    private val callDouble = jEnv.CallDoubleMethodA!!
    private val callObject = jEnv.CallObjectMethodA!!
    private val callVoid = jEnv.CallVoidMethodA!!

    private val callStaticBoolean = jEnv.CallStaticBooleanMethodA!!
    private val callStaticChar = jEnv.CallStaticCharMethodA!!
    private val callStaticByte = jEnv.CallStaticByteMethodA!!
    private val callStaticShort = jEnv.CallStaticShortMethodA!!
    private val callStaticInt = jEnv.CallStaticIntMethodA!!
    private val callStaticLong = jEnv.CallStaticLongMethodA!!
    private val callStaticFloat = jEnv.CallStaticFloatMethodA!!
    private val callStaticDouble = jEnv.CallStaticDoubleMethodA!!
    private val callStaticObject = jEnv.CallStaticObjectMethodA!!
    private val callStaticVoid = jEnv.CallVoidMethodA!!


    override fun call(
        jObject: jobject,
        methodId: jmethodID,
        values: CArrayPointer<jvalue>,
        returnType: JvmBytecodeType
    ): jvalue? {
        val nativeJValue = memAllocator.alloc<jvalue>()
        with(nativeJValue) {
            when (returnType) {
                JvmBytecodeType.B -> b = callByte.invoke(envPtr, jObject, methodId, values)
                JvmBytecodeType.C -> c = callChar.invoke(envPtr, jObject, methodId, values)
                JvmBytecodeType.D -> d = callDouble.invoke(envPtr, jObject, methodId, values)
                JvmBytecodeType.F -> f = callFloat.invoke(envPtr, jObject, methodId, values)
                JvmBytecodeType.I -> i = callInt.invoke(envPtr, jObject, methodId, values)
                JvmBytecodeType.J -> j = callLong.invoke(envPtr, jObject, methodId, values)
                JvmBytecodeType.S -> s = callShort.invoke(envPtr, jObject, methodId, values)
                JvmBytecodeType.Z -> z = callBoolean.invoke(envPtr, jObject, methodId, values)
                JvmBytecodeType.L -> l = callObject.invoke(envPtr, jObject, methodId, values) ?: return null
                JvmBytecodeType.V -> {
                    callVoid.invoke(envPtr, jObject, methodId, values)
                    return null
                }
            }
        }
        return nativeJValue
    }

    override fun callStatic(
        jClass: jclass,
        methodId: jmethodID,
        values: CArrayPointer<jvalue>,
        returnType: JvmBytecodeType
    ): jvalue? {
        val nativeJValue = memAllocator.alloc<jvalue>()
        with(nativeJValue) {
            when (returnType) {
                JvmBytecodeType.B -> b = callStaticByte.invoke(envPtr, jClass, methodId, values)
                JvmBytecodeType.C -> c = callStaticChar.invoke(envPtr, jClass, methodId, values)
                JvmBytecodeType.D -> d = callStaticDouble.invoke(envPtr, jClass, methodId, values)
                JvmBytecodeType.F -> f = callStaticFloat.invoke(envPtr, jClass, methodId, values)
                JvmBytecodeType.I -> i = callStaticInt.invoke(envPtr, jClass, methodId, values)
                JvmBytecodeType.J -> j = callStaticLong.invoke(envPtr, jClass, methodId, values)
                JvmBytecodeType.S -> s = callStaticShort.invoke(envPtr, jClass, methodId, values)
                JvmBytecodeType.Z -> z = callStaticBoolean.invoke(envPtr, jClass, methodId, values)
                JvmBytecodeType.L -> l = callStaticObject.invoke(envPtr, jClass, methodId, values) ?: return null
                JvmBytecodeType.V -> {
                    callStaticVoid.invoke(envPtr, jClass, methodId, values)
                    return null
                }
            }
        }
        return nativeJValue
    }

    override fun getJClass(clazzName: InternalName): jobject = memScoped {
        findClassPtr(envPtr, clazzName.cstr.ptr)!!
    }

    private val objClassCallPtr = jEnv.GetObjectClass!!
    override fun getObjClass(o: jobject): jobject {
        return objClassCallPtr.invoke(envPtr, o)!!
    }

    override val jvalue.obtainO: jobject
        get() = l!!

    override val jobject.obtainV: jvalue
        get() = memAllocator.alloc<jvalue> { l = this@obtainV }

    override val Any.anyAsV: jvalue
        get() = memAllocator.alloc<jvalue> {
            when (val origin = this@anyAsV) {
                is Byte -> b = origin
                is Double -> d = origin
                is Float -> f = origin
                is Int -> i = origin
                is Long -> j = origin
                is Short -> s = origin
                is Boolean -> z = if (origin) 1u else 0u
            }
        }

    override fun getMethodId(
        jClass: jobject, isStatic: Boolean,
        methodName: String, methodDesc: MethodDesc,
    ): jmethodID = memScoped {
        val methodId = if (isStatic) findStaticMethodPtr(
            envPtr, jClass, methodName.cstr.ptr, methodDesc.cstr.ptr
        ) else findMethodPtr(
            envPtr, jClass, methodName.cstr.ptr, methodDesc.cstr.ptr
        )
        methodId!!
    }

    private val getBytesPtr = jEnv.GetByteArrayElements!!
    private val getBytesLengthPtr = jEnv.GetArrayLength!!
    private val createBytesPtr = jEnv.NewByteArray!!
    private val releaseBytesPtr = jEnv.ReleaseByteArrayElements!!
    override fun getBytes(jByteArray: jbyteArray): NativeProto.JBytes = memScoped {
        val jBoolean = alloc<jbooleanVar>()
        jBoolean.value = 0u
        val values = getBytesPtr.invoke(envPtr, jByteArray, jBoolean.ptr)!!
        val length = getBytesLengthPtr.invoke(envPtr, jByteArray)
        NativeProto.JBytes(values, length)
    }

    override fun createJBytes(byteArray: ByteArray): jvalue {
        val length = byteArray.size
        val newArrayPtr = createBytesPtr.invoke(envPtr, length)!!
        val valuesPointer = getBytes(newArrayPtr).valuesPointer
        byteArray.forEachIndexed { index, byte ->
            valuesPointer[index] = byte
        }
        releaseBytes(newArrayPtr, valuesPointer, true)
        val jvalue = memAllocator.alloc<jvalue>()
        jvalue.l = newArrayPtr
        return jvalue
    }

    override fun releaseBytes(jByteArray: jobject, valuesPointer: CArrayPointer<ByteVar>, saveChanges: Boolean) {
        releaseBytesPtr.invoke(envPtr, jByteArray, valuesPointer, if (saveChanges) 0 else JNI_ABORT)
    }

    override fun List<Pair<JvmBytecodeType, jvalue>>.toArrayPtr(): CArrayPointer<jvalue> {
        val array = memAllocator.allocArray<jvalue>(size)
        for ((index, valuePair) in withIndex()) {
            val (bytecodeType, value) = valuePair
            when (bytecodeType) {
                JvmBytecodeType.B -> array[index].b = value.b
                JvmBytecodeType.C -> array[index].c = value.c
                JvmBytecodeType.D -> array[index].d = value.d
                JvmBytecodeType.F -> array[index].f = value.f
                JvmBytecodeType.I -> array[index].i = value.i
                JvmBytecodeType.J -> array[index].j = value.j
                JvmBytecodeType.S -> array[index].s = value.s
                JvmBytecodeType.Z -> array[index].z = value.z
                JvmBytecodeType.L -> array[index].l = value.l
                else -> throw IllegalArgumentException("cannot transform type of [${bytecodeType.jniName}], value: $value")
            }
        }
        return array
    }
}
