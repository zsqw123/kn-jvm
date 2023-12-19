package sample

import zsu.kni.JniApi
import zsu.kni.JniImpl
import zsu.native.demo.calledFromNativeResult

@JniApi
actual external fun nativePlus(a: Int, b: Foo): Bar

@JniApi
actual external fun triggerJvmPlus(a: Int, b: Foo): Bar

@JniImpl
actual fun jvmPlus(a: Int, b: Foo): Bar {
    val result = internalJvmPlus(a, b)
    calledFromNativeResult = result
    return result
}

fun internalJvmPlus(a: Int, b: Foo): Bar {
    return Bar(b.v + a)
}
