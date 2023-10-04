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

### Custom Type

You need to use [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) for custom type support.
We will use [ProtoBuf](https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/README.md#ProtoBuf) as data
transfer format when you want to pass an object as parameter between Kotlin/JVM and Kotlin/Native. Sample below:

```kotlin
@JniShared
expect fun nativePlus(a: Int, b: Foo): Bar

@Serializable // make sure your object is Serializable
class Foo(val v: String)

@Serializable // return type also need Serializable
class Bar(val v: String)

```

When using it, it is completely same with normal usage, and no additional adaptation is required:

```kotlin
fun main() {
    println(nativePlus(1, Foo("f")).v)
}
```

### Performance


## Other

### (WIP) Tech Solution

We use the Lark to write the technical solution, although it is currently only available in Chinese, but you
can choose to use Lark document translator

[From KMM to Project Panama, or Higher](https://eqyrx3fg3l.feishu.cn/docx/K4WQdNDYso6sGTxPmM5c9KVCnYK)

### License

```
Copyright 2023 zsqw123

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
