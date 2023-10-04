package zsu.native.demo

import sample.Foo
import sample.nativePlus
import kotlin.system.measureTimeMillis

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
    val sameFoo = Foo("f")
    val costs = measureTimeMillis {
        repeat(1_000_000) {
            nativePlus(it % 113, sameFoo).v
        }
    }
    println(costs)
}

