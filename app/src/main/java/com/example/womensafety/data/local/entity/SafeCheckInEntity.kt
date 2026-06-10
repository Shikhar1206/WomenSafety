package com.example.womensafety.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "safe_check_ins")
data class SafeCheckInEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val scheduledAt: Long,
    val confirmedAt: Long? = null,
    val status: String = "PENDING",    // PENDING, CONFIRMED, MISSED, SOS_TRIGGERED
    val destination: String? = null,
    val expectedArrivalAt: Long? = null,
    val contactsNotified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
