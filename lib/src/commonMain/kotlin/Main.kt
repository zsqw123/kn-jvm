import platform.posix.sleep
import platform.windows.MessageBox
import platform.windows.MessageBoxA

fun main() {
    println("Hello, Kotlin/Native!")
    MessageBox
}

expect fun showDialog(content: String)