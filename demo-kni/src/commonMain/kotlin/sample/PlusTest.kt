package sample

import kotlinx.serialization.Serializable

expect fun nativePlus(a: Int, b: Foo): Bar

expect fun triggerJvmPlus(a: Int, b: Foo): Bar

expect fun jvmPlus(a: Int, b: Foo): Bar

@Serializable
data class Foo(val v: String)

@Serializable
data class Bar(val v: String)
