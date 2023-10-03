package sample

import zsu.kni.JniImpl

@JniImpl
actual fun nativePlus(a: Int, b: Int): Int {
    return a + b + 2
}