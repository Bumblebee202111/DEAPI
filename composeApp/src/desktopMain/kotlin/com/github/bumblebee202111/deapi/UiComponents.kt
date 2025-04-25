package com.github.bumblebee202111.deapi

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun HexInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Enter(Hex)/Drop Ciphertext") },
        modifier = modifier.fillMaxWidth().height(200.dp),
        isError = isError,
        singleLine = false
    )
}

@Composable
internal fun OutputField(
    value: String,
    modifier: Modifier = Modifier,
    isError: Boolean=false
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text("Result (UTF-8)") },
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        isError=isError,
        singleLine = false
    )
}
