package com.github.bumblebee202111.deapi.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
