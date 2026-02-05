package com.example.tarimobileas.ui

import com.example.tarimobileas.network.Message

data class UiState(
    val models: List<String> = emptyList(),
    val selectedModel: String? = null,
    val chatHistory: List<Message> = emptyList(),
    val isLoading: Boolean = false
)