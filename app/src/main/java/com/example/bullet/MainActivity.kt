package com.example.bullet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bullet.ui.navigation.NibApp
import com.example.bullet.ui.theme.NibTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Let the system manage status/nav bar icon colour based on theme.
        enableEdgeToEdge()
        setContent {
            NibTheme {
                NibApp()
            }
        }
    }
}
