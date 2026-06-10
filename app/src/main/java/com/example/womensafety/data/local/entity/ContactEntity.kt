package com.example.womensafety.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["phone"], unique = true)]
)
data class ContactEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val relation: String = "Emergency Contact",
    val isActive: Boolean = true,
    val avatarUri: String? = null,
    val syncedToFirestore: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
