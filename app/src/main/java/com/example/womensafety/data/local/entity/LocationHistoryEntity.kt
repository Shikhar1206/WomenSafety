package com.example.womensafety.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location_history",
    foreignKeys = [
        ForeignKey(
            entity = SosRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["sosRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sosRecordId")]
)
data class LocationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sosRecordId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float = 0f,
    val recordedAt: Long = System.currentTimeMillis()
)
