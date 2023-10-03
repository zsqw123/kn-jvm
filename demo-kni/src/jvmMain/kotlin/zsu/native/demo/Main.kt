package zsu.native.demo

import sample.nativePlus

@Suppress("UnsafeDynamicallyLoadedCode")
fun main() {
    val obj = object : Any() {}
    val libRes = obj.javaClass.getResource("/zsuDemo.dll")
    checkNotNull(libRes)
    System.load(libRes.path)
    println(nativePlus(1, 2))
}

