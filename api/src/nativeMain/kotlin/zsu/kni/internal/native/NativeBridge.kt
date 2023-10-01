@file:OptIn(ExperimentalForeignApi::class)

package zsu.kni.internal.native

import kotlinx.cinterop.*

typealias JObjectStub = CPointer<*>
typealias JClassStub = CPointer<*>
typealias JEnvStub = CPointer<*>
typealias JMethodIdStub = CPointer<*>

@OptIn(ExperimentalForeignApi::class)
class NativeBridge(val proto: NativeProto<JObjectStub, CPointed, JMethodIdStub>) {
    fun callStatic(clazz: String, method: String, args: List<CPointed>): CPointed {
        proto.callStatic()
    }

    // get bytes from java byte array
//     fun getByteArray(jByteArray: CPointed): ByteArray

    // create java byte array object from native bytes
//     fun toByteArray(nativeBytes: ByteArray): CPointed

    fun getArgsPointer(args: List<CPointer<*>>): CArrayPointer<out CPointed> = memScoped {
        allocArrayOf(args)
    }

    fun getArgs(args: List<CPointed>): CArrayPointer<out CPointed> {
        val pointers = args.map { it.ptr }
        memScoped {
            allocArrayOf(pointers)
        }
    }
}
