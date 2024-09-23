package zsu.native.demo

import sample.*
import zsu.kni.JniApi
import zsu.kni.KniNativeThread
import java.io.File
import kotlin.io.path.createTempDirectory

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
    KniNativeThread.attach(JniApi.DEFAULT)

    // jvm -> native
    val expect0 = "native: f, 1"
    val actual0 = nativePlus(1, Foo("f")).v
    println(actual0)
    assert(expect0 == actual0) {
        "expect: $expect0, actual: $actual0"
    }

    // native -> jvm
    val triggered = triggerJvmPlus(1, Foo("2"))
    println(triggered)
    assert(triggered.v == "called") {
        "expect: called, actual: ${triggered.v}"
    }
    val expectJvmResult = internalJvmPlus(1, Foo("2"))
    val actualResult = calledFromNativeResult
    println(actualResult)
    assert(expectJvmResult == actualResult) {
        "expect: $expectJvmResult, actual: $actualResult"
    }

    println("all passed")
}

@Volatile
var calledFromNativeResult: Bar? = null
