package sample

import zsu.kni.JniImpl

@JniImpl
actual fun nativePlus(a: Int, b: Foo): Bar {
    return Bar("native: ${b.v}, $a")
}

actual fun jvmPlus(a: Int, b: Foo): Bar {
    TODO("Not yet implemented")
}
