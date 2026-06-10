package com.example.womensafety.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.womensafety.presentation.auth.AuthViewModel
import com.example.womensafety.presentation.auth.ForgotPasswordScreen
import com.example.womensafety.presentation.auth.LoginScreen
import com.example.womensafety.presentation.auth.SignUpScreen
import com.example.womensafety.presentation.contacts.ContactsScreen
import com.example.womensafety.presentation.home.HomeScreen
import com.example.womensafety.presentation.map.MapScreen
import com.example.womensafety.presentation.settings.SettingsScreen
import com.example.womensafety.presentation.sos.SosHistoryScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Flow
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Main App ─────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToHistory = { navController.navigate(Screen.SosHistory.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToFakeCall = { navController.navigate(Screen.FakeCall.route) }
            )
        }

        composable(Screen.Contacts.route) {
            ContactsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SosHistory.route) {
            SosHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
