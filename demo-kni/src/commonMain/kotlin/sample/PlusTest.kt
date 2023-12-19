package sample

import kotlinx.serialization.Serializable
import zsu.kni.JniShared

@JniShared
expect fun nativePlus(a: Int, b: Foo): Bar

@JniShared
expect fun triggerJvmPlus(a: Int, b: Foo): Bar

@JniShared
expect fun jvmPlus(a: Int, b: Foo): Bar

@Serializable
data class Foo(val v: String)

@Serializable
data class Bar(val v: String)
