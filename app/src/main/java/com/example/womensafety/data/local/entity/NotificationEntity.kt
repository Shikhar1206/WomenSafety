package com.example.womensafety.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val type: String,    // SOS, CHECK_IN, REMINDER, ALERT
    val isRead: Boolean = false,
    val receivedAt: Long = System.currentTimeMillis()
)
