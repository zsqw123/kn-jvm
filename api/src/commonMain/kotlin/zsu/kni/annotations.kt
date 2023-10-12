package zsu.kni

/**
 * mark a function can split api/impl in different platforms.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class JniShared

/**
 * mark a function as an api function, it will be implemented in another platform.
 *
 * @property threadId it used for invoke java from native, must register needed id before call it.
 *  you can register a thread through `KniNativeThread.attach` in jvm platform. This parameter
 *  is useless when mark it for jvm platform functions
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class JniApi(val threadId: Int = DEFAULT) {
    companion object {
        const val DEFAULT = 0
    }
}

/**
 * mark a function as an impl function, it will be called by other platform.
 * it is the implementation for [JniApi] marked function.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class JniImpl
