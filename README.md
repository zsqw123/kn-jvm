# KN-JVM

![preview](docs/sample.png)

Auto JNI binding based on Kotlin Multiplatform. Just need simply declare functions in the common module, and the tool
will automatically generate the corresponding Kotlin Native code (based on KSP)

## Usage

```kotlin
// 1. declare shared api in common module
@JniShared
expect fun nativePlus(a: Int, b: Int): Int

// 2. declare api/impl in jvm/native sourceset
@JniApi // jvm
actual external fun nativePlus(a: Int, b: Int): Int

@JniImpl // native
actual fun nativePlus(a: Int, b: Int): Int {
    return a + b + 2 // plus 2 makes you believe this from native
}

// 3. load native lib and call the jvm api function
fun main() {
    // load native sourceset as a lib by yourself
    // loadLib()
    println(nativePlus(1, 2)) // 5
}
```

