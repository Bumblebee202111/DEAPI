package com.github.bumblebee202111.deapi

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.bumblebee202111.deapi.composables.HexInputField
import com.github.bumblebee202111.deapi.composables.OutputField
import com.github.bumblebee202111.deapi.crypto.CryptoUtils
import com.github.bumblebee202111.deapi.gzip.GzipUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalStdlibApi::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    var hexInput by remember { mutableStateOf("") }
    var outputResult by remember { mutableStateOf(Result.success("")) }
    var useGzip by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val performDecryption: suspend (ByteArray, Boolean) -> Result<String> = { bytes, shouldUnGzip ->
        withContext(Dispatchers.Default) {
            try {
                val decryptedBytes = CryptoUtils.decrypt(bytes)
                val finalBytes = if (shouldUnGzip) {
                    try {
                        GzipUtils.decompress(decryptedBytes)
                    } catch (e: Exception) {
                        throw RuntimeException("Gzip decompression failed: ${e.message}", e)
                    }
                } else {
                    decryptedBytes
                }
                Result.success(String(finalBytes, Charsets.UTF_8))
            } catch (e: Exception) {
                Result.failure(RuntimeException("Operation Failed: ${e.message}", e))
            }
        }
    }

    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) { isDragging = true }
            override fun onEnded(event: DragAndDropEvent) { isDragging = false }
            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragging = false
                (event.awtTransferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>)
                    ?.firstOrNull()
                    ?.let { file ->
                        if (file is File && file.isFile) {
                            coroutineScope.launch {
                                val fileBytes = withContext(Dispatchers.IO) { file.readBytes() }
                                hexInput = fileBytes.toHexString(HexFormat.UpperCase)
                                outputResult = performDecryption(fileBytes, useGzip)
                            }
                        }
                    }
                return true
            }
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { it.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) },
                    target = dragAndDropTarget
                )
                .background(if (isDragging) Color.LightGray else MaterialTheme.colors.background)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("DEAPI - EAPI Decryptor", style = MaterialTheme.typography.h5)

                HexInputField(
                    value = hexInput,
                    onValueChange = { hexInput = it },
                    isError = outputResult.isFailure
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Checkbox(checked = useGzip, onCheckedChange = { useGzip = it })
                    Text("Use Gzip Decompression (for `x-aeapi` payloads)")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val inputBytes = hexInput.replace("\\s".toRegex(), "").hexToByteArray()
                                outputResult = performDecryption(inputBytes, useGzip)
                            } catch (e: Exception) {
                                outputResult = Result.failure(IllegalArgumentException("Invalid Hex Input: ${e.message}", e))
                            }
                        }
                    },
                    enabled = hexInput.isNotBlank()
                ) {
                    Text("Decrypt")
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