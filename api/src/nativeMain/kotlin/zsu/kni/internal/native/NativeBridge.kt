package zsu.kni.internal.native

/**
 * @param T type of jobject, but it is different for different platform
 */
interface NativeBridge<T> {
    fun callStatic(clazz: String, method: String, args: List<T>)
}