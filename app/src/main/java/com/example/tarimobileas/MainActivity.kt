package com.example.tarimobileas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.tarimobileas.ui.AppNavHost
import com.example.tarimobileas.ui.TariViewModel
import com.example.tarimobileas.ui.theme.TariMobileASTheme

class MainActivity : ComponentActivity() {


    private val viewModel: TariViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            TariMobileASTheme {
                AppNavHost(viewModel = viewModel)
            }
        }
    }
}
