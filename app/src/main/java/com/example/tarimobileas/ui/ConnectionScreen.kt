package com.example.tarimobileas.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarimobileas.R
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ConnectionScreen(
    defaultHost: String,
    onConnect: (String) -> Unit,
    connectionError: String?
) {
    var host by remember { mutableStateOf(defaultHost) }

    // ----- obtain the ViewModel just for its list of preset hosts -----
    val vm = viewModel<TariViewModel>()

    // ----- dropdown state ------------------------------------------------
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------- Banner image ----------
            Image(
                painter = painterResource(id = R.drawable.model_banner),
                contentDescription = "Connection banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------- Welcome text ----------
            Text(
                text = "Hallo wie geht? :)",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- Preset‑IP dropdown ----------
            OutlinedButton(onClick = { expanded = true }) {
                Text("Select preset")
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                vm.defaultHosts.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset) },
                        onClick = {
                            host = preset
                            expanded = false
                        })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------- IP address input ----------
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("LM‑Studio IP address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- Connect button ----------
            Button(
                onClick = { onConnect(host.trim()) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Connect")
            }

            // ---------- Optional error message ----------
            connectionError?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = Color.Red)
            }
        }
    }
}
