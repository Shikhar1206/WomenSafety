package com.example.womensafety.domain.model

data class SosRecord(
    val id: String,
    val triggeredAt: Long,
    val triggeredBy: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationAddress: String?,
    val smsSentCount: Int,
    val status: String,
    val cancelledAt: Long? = null
)
