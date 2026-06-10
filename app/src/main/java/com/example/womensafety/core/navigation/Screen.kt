package com.example.womensafety.core.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")

    // Main
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object SosHistory : Screen("sos_history")
    object Map : Screen("map")
    object Settings : Screen("settings")
    object FakeCall : Screen("fake_call")
}
