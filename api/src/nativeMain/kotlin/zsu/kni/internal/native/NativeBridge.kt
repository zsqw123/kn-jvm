package zsu.kni.internal.native

/**
 * @param T type of jobject, but it is different for different platform
 */
interface NativeBridge<T> {
    fun callStatic(clazz: String, method: String, args: List<T>)

    // get bytes from java byte array
    fun getByteArray(jByteArray: T): ByteArray

    // create java byte array object from native bytes
    fun toByteArray(nativeBytes: ByteArray): T
}