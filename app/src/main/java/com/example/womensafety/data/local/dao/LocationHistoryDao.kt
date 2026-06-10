package com.example.womensafety.data.local.dao

import androidx.room.*
import com.example.womensafety.data.local.entity.LocationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryDao {

    @Insert
    suspend fun insert(location: LocationHistoryEntity)

    @Query("SELECT * FROM location_history WHERE sosRecordId = :sosId ORDER BY recordedAt ASC")
    fun getLocationsBySosId(sosId: String): Flow<List<LocationHistoryEntity>>

    @Query("SELECT * FROM location_history ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLastKnownLocation(): LocationHistoryEntity?

    @Query("DELETE FROM location_history WHERE sosRecordId = :sosId")
    suspend fun deleteForSosRecord(sosId: String)
}
