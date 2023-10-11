package zsu.kni

object KniNativeThread {
    fun attach() {
        attachCurrentThread(Thread.currentThread().id)
    }

    fun detach() {
        detachCurrentThread(Thread.currentThread().id)
    }

    fun detachAll() {
        detachAllThread()
    }

    @JvmStatic
    private external fun attachCurrentThread(threadId: Long)

    @JvmStatic
    private external fun detachCurrentThread(threadId: Long)

    @JvmStatic
    private external fun detachAllThread()
}