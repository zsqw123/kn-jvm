package lib

import javafx.application.Application
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class EntryApp : Application() {
    override fun start(primaryStage: Stage) {
        val content = parameters.raw.first()
        val javaVersion = System.getProperty("java.version")
        val javafxVersion = System.getProperty("javafx.version")
        val l = Label("$content\nKotlin JavaFX $javafxVersion, running on Java $javaVersion.")
        val scene = Scene(StackPane(l), 640.0, 480.0)
        primaryStage.setScene(scene)
        primaryStage.show()
    }
}
