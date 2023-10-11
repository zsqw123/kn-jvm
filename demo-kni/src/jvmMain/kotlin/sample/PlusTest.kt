package sample

import zsu.kni.JniApi

@JniApi
actual external fun nativePlus(a: Int, b: Foo): Bar

actual fun jvmPlus(a: Int, b: Foo): Bar {
    return Bar("jvm: $a -> ${b.v}")
}
