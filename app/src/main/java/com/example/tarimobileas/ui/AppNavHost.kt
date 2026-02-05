package com.example.tarimobileas.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.*

@Composable
fun AppNavHost(viewModel: TariViewModel) {
    val navController = rememberNavController()

    // UI state that drives navigation
    val uiState by viewModel.uiState.collectAsState()
    var connectionError by remember { mutableStateOf<String?>(null) }

    NavHost(navController, startDestination = "connect") {

        composable("connect") {
            ConnectionScreen(
                defaultHost = viewModel.currentHost,
                onConnect = { enteredHost ->
                    viewModel.tryConnect(
                        hostInput = enteredHost,
                        onSuccess = { navController.navigate("models") },
                        onFailure = { msg -> connectionError = msg }
                    )
                },
                connectionError = connectionError
            )
        }

        composable("models") {
            ModelPickerScreen(
                state = uiState,
                onSelect = { modelId ->
                    viewModel.selectModel(modelId)
                    navController.navigate("chat")
                },
                viewModel = viewModel
            )
        }

        composable("chat") {
            ChatScreen(
                state = uiState,
                onSend = { text -> viewModel.sendMessage(text) },

                onExport = {},
                onClear = { viewModel.clearChat() }
            )
        }
    }
}
