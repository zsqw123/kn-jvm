package sample

import kotlinx.serialization.Serializable
import zsu.kni.JniShared

@JniShared
expect fun nativePlus(a: Int, b: Foo): Bar

@JniShared
expect fun jvmPlus(a: Int, b: Foo): Bar

@Serializable
class Foo(val v: String)

@Serializable
class Bar(val v: String)
