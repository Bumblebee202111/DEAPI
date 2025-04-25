package com.github.bumblebee202111.deapi


import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(width = 700.dp, height = 650.dp)

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "DEAPI",
    ) {
        App()
    }
}