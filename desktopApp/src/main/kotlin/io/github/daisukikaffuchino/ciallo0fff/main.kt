package io.github.daisukikaffuchino.ciallo0fff

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main(args: Array<String>) {
    if ("--test-environment" in args) {
        System.setProperty("ciallo0fff.testEnvironment", "true")
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = 960.dp, height = 720.dp),
            title = "Ciallo0FFF",

            icon = painterResource("ic_launcher_linux.png"),
        ) {
            window.minimumSize = java.awt.Dimension(420, 640)
            App()
        }
    }
}
