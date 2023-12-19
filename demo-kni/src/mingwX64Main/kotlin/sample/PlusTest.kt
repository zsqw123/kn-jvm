package sample

import zsu.kni.JniApi
import zsu.kni.JniImpl

@JniImpl
actual fun nativePlus(a: Int, b: Foo): Bar {
    return Bar("native: ${b.v}, $a")
}

@JniImpl
actual fun triggerJvmPlus(a: Int, b: Foo): Bar {
    jvmPlus(a, b)
    return Bar("called")
}

@JniApi
actual fun jvmPlus(a: Int, b: Foo): Bar {
    return jvmPlusImpl(a, b)
}
