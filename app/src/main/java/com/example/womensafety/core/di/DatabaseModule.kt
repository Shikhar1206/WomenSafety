package com.example.womensafety.core.di

import android.content.Context
import androidx.room.Room
import com.example.womensafety.data.local.dao.ContactDao
import com.example.womensafety.data.local.dao.LocationHistoryDao
import com.example.womensafety.data.local.dao.NotificationDao
import com.example.womensafety.data.local.dao.SosRecordDao
import com.example.womensafety.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "women_safety_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideContactDao(db: AppDatabase): ContactDao = db.contactDao()

    @Provides
    @Singleton
    fun provideSosRecordDao(db: AppDatabase): SosRecordDao = db.sosRecordDao()

    @Provides
    @Singleton
    fun provideLocationHistoryDao(db: AppDatabase): LocationHistoryDao = db.locationHistoryDao()

    @Provides
    @Singleton
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
}
