package sample

import zsu.kni.JniApi
import zsu.kni.JniImpl

@JniApi
actual external fun nativePlus(a: Int, b: Foo): Bar

@JniImpl
actual fun jvmPlus(a: Int, b: Foo): Bar {
    return Bar(b.v + a)
}
