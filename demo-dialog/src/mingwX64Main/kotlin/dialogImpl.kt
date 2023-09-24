import kotlinx.cinterop.*
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
actual fun showDialog(content: String): Unit = memScoped {
    val infoBuf = UShortArray(MAX_COMPUTERNAME_LENGTH + 1)
    val infoPtr = infoBuf.refTo(0).getPointer(this)

    val bufPtr: LPDWORD = alloc<DWORDVar>().ptr
    bufPtr[0] = (MAX_COMPUTERNAME_LENGTH + 1).toUInt()
    GetComputerNameW(infoPtr, bufPtr)

    val name = infoPtr.toKString()
    MessageBoxW(null, "$content\ncomputerName: $name!", content, MB_OK.toUInt())
}