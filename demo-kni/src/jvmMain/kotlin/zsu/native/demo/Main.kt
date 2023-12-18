package zsu.native.demo

import sample.Foo
import sample.nativePlus
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

    val expect0 = "native: f, 1"
    val actual0 = nativePlus(1, Foo("f")).v
    assert(expect0 == actual0) {
        "expect: $expect0, actual: $actual0"
    }
    var calledFromNative = false
    while (!calledFromNative) {

    }
}

