package com.example.womensafety.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sos_records")
data class SosRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val triggeredAt: Long = System.currentTimeMillis(),
    val triggeredBy: String,          // MANUAL, SHAKE, VOICE, BATTERY
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAddress: String? = null,
    val smsSentCount: Int = 0,
    val status: String = "ACTIVE",    // ACTIVE, CANCELLED, RESOLVED
    val syncedToFirestore: Boolean = false,
    val cancelledAt: Long? = null
)
