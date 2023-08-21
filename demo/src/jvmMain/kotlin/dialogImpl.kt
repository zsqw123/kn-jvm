import javafx.application.Application
import lib.EntryApp

actual fun showDialog(content: String) {
    Application.launch(EntryApp::class.java, content)
}