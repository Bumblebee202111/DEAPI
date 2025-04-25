package com.github.bumblebee202111.deapi

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.bumblebee202111.deapi.crypto.CryptoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(
    ExperimentalStdlibApi::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
@Preview
fun App() {
    var hexInput by remember { mutableStateOf("") }
    var outputResult by remember { mutableStateOf<Result<String>>(Result.success("")) }
    var isDragging by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val performDecryption: suspend (ByteArray) -> Result<String> = { bytes ->
        withContext(Dispatchers.Default) {
            try {
                val decryptedText = CryptoUtils.decrypt(bytes)
                Result.success(decryptedText)
            } catch (e: Exception) {
                Result.failure(RuntimeException("Decryption Error: ${e.message}", e))
            }
        }
    }
    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isDragging = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDragging = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragging = false
                try {
                    val transferable = event.awtTransferable
                    val files =
                        transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    val file = files.firstOrNull() as? File

                    when {
                        file == null -> {
                            outputResult =
                                Result.failure(RuntimeException("No file found in drop data"))
                        }

                        !file.isFile -> {
                            outputResult =
                                Result.failure(RuntimeException("Please drop a single file"))
                        }

                        else -> {
                            coroutineScope.launch {
                                try {
                                    val fileBytes =
                                        withContext(Dispatchers.IO) { file.readBytes() }
                                    hexInput = fileBytes.toHexString(HexFormat.UpperCase)
                                    outputResult = performDecryption(fileBytes)

                                } catch (e: Exception) {
                                    outputResult =
                                        Result.failure(RuntimeException("Error reading file: ${e.message}"))
                                }
                            }


                        }
                    }
                } catch (e: Exception) {
                    outputResult = Result.failure(
                        RuntimeException(
                            "Error handling drop event: ${e.message}",
                            e
                        )
                    )
                }
                return true
            }
        }
    }

    MaterialTheme {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize()
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { event ->
                            val transferable = event.awtTransferable
                            transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                        },
                        target = dragAndDropTarget
                    )
                    .background(if (isDragging) Color.LightGray else MaterialTheme.colors.background)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("DEAPI - An EAPI Decryptor", style = MaterialTheme.typography.h5)

                    HexInputField(
                        value = hexInput,
                        onValueChange = { hexInput = it },
                        isError = outputResult.exceptionOrNull() is IllegalArgumentException
                    )

                    Button(
                        onClick = {
                            if (hexInput.isBlank()) {
                                outputResult = Result.success("")
                            } else if (hexInput.any { !it.isHexDigit() && !it.isWhitespace() }) {
                                outputResult = Result.failure(
                                    IllegalArgumentException("Invalid Hex Input: Contains non-hexadecimal characters.")
                                )
                            } else {
                                coroutineScope.launch {
                                    try {
                                        val inputBytes = hexInput.trim().hexToByteArray()
                                        outputResult = performDecryption(inputBytes)
                                    } catch (e: Exception) {
                                        outputResult = Result.failure(
                                            RuntimeException(
                                                "Input Processing Error: ${e.message}",
                                                e
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        enabled = hexInput.isNotBlank()
                    ) {
                        Text("Decrypt Input")
                    }

                    OutputField(
                        value = outputResult.fold(
                            onSuccess = { it },
                            onFailure = { it.message ?: "Unknown Error" },
                        ),
                        modifier = Modifier.weight(1f),
                        isError = outputResult.isFailure
                    )
                }
            }
        }
    }
}

fun Char.isHexDigit() =
    this.isDigit() || this in 'A'..'F' || this in 'a'..'f' // Accept lowercase for compatibility