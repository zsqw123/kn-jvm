package sample

import kotlinx.serialization.Serializable

expect fun nativePlus(a: Int, b: Foo): Bar

@Serializable
class Foo(val v: String)

@Serializable
class Bar(val v: String)
