package com.example.wasmapplication.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wasmapplication.ui.theme.WasmApplicationTheme


@Composable
fun BasicButton(onClick: () -> Unit, text: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal=20.dp) // يملأ كامل الشاشة
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
                .height(50.dp)
                .align(Alignment.Center)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(text = text)
        }
    }
}
