package com.example.tarimobileas.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel



@Composable
fun MainScreen(
    viewModel: TariViewModel = viewModel(),
    onExportRequested: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.selectedModel) {
        null -> ModelPickerScreen(
            state = uiState,
            onSelect = { modelId -> viewModel.selectModel(modelId) },
            viewModel = viewModel
        )
        else -> ChatScreen(
            state = uiState,
            onSend = { text -> viewModel.sendMessage(text) },
            onExport = onExportRequested,
            onClear = { viewModel.clearChat() }
        )
    }
}


