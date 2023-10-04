package zsu.native.demo

import sample.Foo
import sample.nativePlus
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.system.measureTimeMillis

@Suppress("UnsafeDynamicallyLoadedCode")
fun loadDemoLib() {
    val obj = object : Any() {}
    val libRes = obj.javaClass.getResource("/zsuDemo.dll")
    checkNotNull(libRes)
    val d = createTempDirectory().toFile()
    val tmpLib = File(d, "zsuDemo.dll")
    if (tmpLib.exists()) tmpLib.delete()
    tmpLib.createNewFile()
    libRes.openStream().use { input ->
        tmpLib.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    System.load(tmpLib.absolutePath)
}

fun main() {
    loadDemoLib()
    println(nativePlus(1, Foo("f")).v)
    val sameFoo = Foo("f")
    val costs = measureTimeMillis {
        repeat(1_000_000) {
            nativePlus(it % 113, sameFoo).v
        }
    }
    println(costs)
}

