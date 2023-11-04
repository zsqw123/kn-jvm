package org.example

import sample.Bar
import sample.Foo
import sample.nativePlus
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.system.exitProcess



fun simpleTask(benchmark: Benchmark) {
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

fun directCallStub(a: Int, b: Foo): Bar {
    return Bar("native: ${b.v}, $a") // impl by jvm
}

fun directCallTask(benchmark: Benchmark) {
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
        directCallStub(current, currentFoo)
    }
    exitProcess(0)
}
