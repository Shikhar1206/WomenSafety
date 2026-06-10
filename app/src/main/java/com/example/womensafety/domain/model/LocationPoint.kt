package com.example.womensafety.domain.model

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val recordedAt: Long = System.currentTimeMillis()
) {
    fun toMapsLink(): String = "https://maps.google.com/?q=$latitude,$longitude"
}
