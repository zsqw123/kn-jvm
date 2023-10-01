@file:OptIn(ExperimentalForeignApi::class)

package zsu.kni.test

import kotlinx.cinterop.*
import zsu.jni.*
import zsu.kni.internal.BytecodeName
import zsu.kni.internal.JvmBytecodeType
import zsu.kni.internal.JvmBytecodeType.*
import zsu.kni.internal.MethodDesc
import zsu.kni.internal.native.NativeProto

// just my test file :)
@OptIn(ExperimentalForeignApi::class)
class TestNativeProto(private val envPtr: CPointer<JNIEnvVar>) : NativeProto<jobject, jvalue, jmethodID> {
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
        val nativeJValue = nativeHeap.alloc<jvalue>()
        with(nativeJValue) {
            when (returnType) {
                B -> b = callByte.invoke(envPtr, jObject, methodId, values)
                C -> c = callChar.invoke(envPtr, jObject, methodId, values)
                D -> d = callDouble.invoke(envPtr, jObject, methodId, values)
                F -> f = callFloat.invoke(envPtr, jObject, methodId, values)
                I -> i = callInt.invoke(envPtr, jObject, methodId, values)
                J -> j = callLong.invoke(envPtr, jObject, methodId, values)
                S -> s = callShort.invoke(envPtr, jObject, methodId, values)
                Z -> z = callBoolean.invoke(envPtr, jObject, methodId, values)
                L -> l = callObject.invoke(envPtr, jObject, methodId, values)
                V -> {
                    nativeHeap.free(nativeJValue)
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
        val nativeJValue = nativeHeap.alloc<jvalue>()
        with(nativeJValue) {
            when (returnType) {
                B -> b = callStaticByte.invoke(envPtr, jClass, methodId, values)
                C -> c = callStaticChar.invoke(envPtr, jClass, methodId, values)
                D -> d = callStaticDouble.invoke(envPtr, jClass, methodId, values)
                F -> f = callStaticFloat.invoke(envPtr, jClass, methodId, values)
                I -> i = callStaticInt.invoke(envPtr, jClass, methodId, values)
                J -> j = callStaticLong.invoke(envPtr, jClass, methodId, values)
                S -> s = callStaticShort.invoke(envPtr, jClass, methodId, values)
                Z -> z = callStaticBoolean.invoke(envPtr, jClass, methodId, values)
                L -> l = callStaticObject.invoke(envPtr, jClass, methodId, values)
                V -> {
                    nativeHeap.free(nativeJValue)
                    callStaticVoid.invoke(envPtr, jClass, methodId, values)
                    return null
                }
            }
        }
        return nativeJValue
    }

    override fun getJClass(clazzName: BytecodeName): jobject = memScoped {
        findClassPtr(envPtr, clazzName.cstr.ptr)!!
    }

    private val objClassCallPtr = jEnv.GetObjectClass!!
    override fun getObjClass(o: jobject): jobject {
        return objClassCallPtr.invoke(envPtr, o)!!
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
        jBoolean.value = JNI_FALSE.toUByte()
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
        val jvalue = nativeHeap.alloc<jvalue>()
        jvalue.l = newArrayPtr
        return jvalue
    }

    override fun releaseBytes(jByteArray: jobject, valuesPointer: CArrayPointer<ByteVar>, saveChanges: Boolean) {
        releaseBytesPtr.invoke(envPtr, jByteArray, valuesPointer, if (saveChanges) 0 else JNI_ABORT)
    }

    override fun List<Pair<JvmBytecodeType, jvalue>>.toArrayPtr(): CArrayPointer<jvalue> {
        val array = nativeHeap.allocArray<jvalue>(size)
        for ((index, valuePair) in withIndex()) {
            val (bytecodeType, value) = valuePair
            when (bytecodeType) {
                B -> array[index].b = value.b
                C -> array[index].c = value.c
                D -> array[index].d = value.d
                F -> array[index].f = value.f
                I -> array[index].i = value.i
                J -> array[index].j = value.j
                S -> array[index].s = value.s
                Z -> array[index].z = value.z
                L -> array[index].l = value.l
                else -> throw IllegalArgumentException("cannot transform type of [${bytecodeType.jniName}], value: $value")
            }
        }
        return array
    }
}