package com.example.womensafety.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.womensafety.data.local.dao.ContactDao
import com.example.womensafety.data.local.dao.LocationHistoryDao
import com.example.womensafety.data.local.dao.NotificationDao
import com.example.womensafety.data.local.dao.SosRecordDao
import com.example.womensafety.data.local.entity.ContactEntity
import com.example.womensafety.data.local.entity.LocationHistoryEntity
import com.example.womensafety.data.local.entity.NotificationEntity
import com.example.womensafety.data.local.entity.SafeCheckInEntity
import com.example.womensafety.data.local.entity.SosRecordEntity

@Database(
    entities = [
        ContactEntity::class,
        SosRecordEntity::class,
        LocationHistoryEntity::class,
        SafeCheckInEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun sosRecordDao(): SosRecordDao
    abstract fun locationHistoryDao(): LocationHistoryDao
    abstract fun notificationDao(): NotificationDao
}
