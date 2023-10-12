package zsu.kni

object KniNativeThread {
    /**
     * @param id custom id for current thread
     */
    @JvmStatic
    external fun attach(id: Int)

    /**
     * @param id Int
     * @param sameJEnv Boolean, keep same jenv with attached. 0->false; other->true
     */
    @JvmStatic
    external fun detach(id: Int, sameJEnv: Int)

    // detach without env check
    fun detach(id: Int) = detach(id, 0)

    @JvmStatic
    external fun detachAll()
}