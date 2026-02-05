package com.example.tarimobileas.ui




import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable

import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme



/* -------------------------------------------------------------
   1️⃣ Model picker screen – shows list of models returned by LM‑Studio
   ------------------------------------------------------------- */
@Composable
fun ModelPickerScreen(
    state: UiState,
    onSelect: (String) -> Unit,
    viewModel: TariViewModel
) {
    val displayedModels = state.models.filter { it in viewModel.WHITELIST.keys }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Choose a model",
                fontSize = 24.sp,
                lineHeight = 32.sp,
                modifier = Modifier.padding(all = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(displayedModels.size) { index ->
                    val modelId = displayedModels[index]
                    val friendlyName = viewModel.WHITELIST[modelId] ?: modelId
                    Text(
                        text = friendlyName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(modelId) }
                            .padding(8.dp)
                    )
                }
            }

            if (displayedModels.isEmpty()) {
                Text(
                    "No whitelisted models available.\nCheck your LM‑Studio server.",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}


/* -------------------------------------------------------------
   2️⃣ Chat screen – shows conversation and a send box
   ------------------------------------------------------------- */
@Composable
fun ChatScreen(
    state: UiState,
    onSend: (String) -> Unit,
    onExport: () -> Unit,
    onClear: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    Scaffold(

        modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ----- chat history (scrollable) -----
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = false,
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(state.chatHistory.size) { idx ->
                    val msg = state.chatHistory[idx]
                    val displayName = friendlyRole(msg.role)
                    Text("$displayName: ${msg.content}", modifier = Modifier.padding(4.dp))
                }
            }

            // ----- input row (Send / Export / Clear) -----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Enter message…") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (input.isNotBlank()) {
                        onSend(input.trim())
                        input = ""
                    }
                }) { Text("Send") }


                var menuExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("Export") }, onClick = {
                        onExport(); menuExpanded = false })
                    DropdownMenuItem(text = { Text("Clear") }, onClick = {
                        onClear(); menuExpanded = false })
                }
            }
        }
    }
}

private fun friendlyRole(raw: String): String = when (raw.lowercase()) {
    "user"      -> "Nini"
    "assistant" -> "Tari"
    else        -> raw.capitalize()
}