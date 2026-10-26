package com.example.contador.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun ContadorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}