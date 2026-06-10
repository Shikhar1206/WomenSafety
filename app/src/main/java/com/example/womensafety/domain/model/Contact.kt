package com.example.womensafety.domain.model

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String = "Emergency Contact",
    val isActive: Boolean = true,
    val avatarUri: String? = null
)
