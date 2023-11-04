package org.example

import zsu.native.demo.loadDemoLib

fun main() {
    loadDemoLib()
    val benchmark = Benchmark(
        singleProcessTime = 5,
        warmupCount = 2,
        processCount = 10
    )
//    simpleTask(benchmark)
    directCallTask(benchmark)
}
