package org.example

import kotlinx.coroutines.*

typealias Seconds = Int
typealias MS = Long

class Benchmark(
    private val singleProcessTime: Seconds,
    private val warmupCount: Int,
    private val processCount: Int,
) {
    fun run(block: () -> Unit) = runBlocking {
        avg(warmupCount) {
            runSingleProcess(block)
        }.print("warmup")
        avg(processCount) {
            runSingleProcess(block)
        }.print("realProcess")
    }


    private fun CoroutineScope.timed(timeMs: MS, timeEnd: () -> Unit) {
        launch(Dispatchers.Default) {
            delay(timeMs)
            timeEnd()
        }
    }

    private inline fun avg(count: Int, action: () -> SingleResult): SingleResult {
        val results = (1..count).map {
            println("Process $it...")
            action()
        }
        val allTime = results.sumOf { it.executeTime }
        val allCount = results.sumOf { it.executeCount }
        return SingleResult(allTime, allCount)
    }

    private fun CoroutineScope.runSingleProcess(block: () -> Unit): SingleResult {
        var executes = 0L
        var processEnd = false
        timed(singleProcessTime * 1000L) {
            processEnd = true
        }
        while (true) {
            block()
            executes++
            if (processEnd) break
        }
        return SingleResult(singleProcessTime, executes)
    }

    class SingleResult(
        val executeTime: Seconds,
        val executeCount: Long,
    ) {
        fun print(name: String) {
            val ops = executeCount.toDouble() / executeTime
            val opsStr = String.format("%.2f", ops)
            println("[$name] $opsStr op/s ")
        }
    }
}
