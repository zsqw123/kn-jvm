package org.example

import sample.Foo
import sample.nativePlus
import zsu.native.demo.loadDemoLib
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.system.exitProcess

fun main() {
    loadDemoLib()
    val benchmark = Benchmark(
        singleProcessTime = 5,
        warmupCount = 2,
        processCount = 10
    )
    val random = Random(System.currentTimeMillis())
    var current = 1
    var currentFoo = Foo("v")
    thread {
        while (true) {
            current = random.nextInt(-100_000, 100_000)
            currentFoo = Foo(current.toString())
        }
    }
    benchmark.run {
        nativePlus(current, currentFoo)
    }
    exitProcess(0)
}
