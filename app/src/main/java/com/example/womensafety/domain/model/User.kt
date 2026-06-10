package com.example.womensafety.domain.model

data class User(
    val uid: String,
    val email: String,
    val displayName: String?,
    val phoneNumber: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean = false
)
