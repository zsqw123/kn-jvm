package zsu.native.demo

import sample.Foo
import sample.nativePlus

@Suppress("UnsafeDynamicallyLoadedCode")
private fun loadLib() {
    val obj = object : Any() {}
    val libRes = obj.javaClass.getResource("/zsuDemo.dll")
    checkNotNull(libRes)
    System.load(libRes.path)
}

fun main() {
    loadLib()
    println(nativePlus(1, Foo("f")).v)
}

