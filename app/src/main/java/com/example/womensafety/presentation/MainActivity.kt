package com.example.womensafety.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.womensafety.core.navigation.NavGraph
import com.example.womensafety.core.navigation.Screen
import com.example.womensafety.presentation.auth.AuthViewModel
import com.example.womensafety.presentation.theme.WomenSafetyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WomenSafetyTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                val startDestination = if (isLoggedIn) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
