package com.example.womensafety.data.local.dao

import androidx.room.*
import com.example.womensafety.data.local.entity.SosRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SosRecordDao {

    @Insert
    suspend fun insert(record: SosRecordEntity)

    @Update
    suspend fun update(record: SosRecordEntity)

    @Query("SELECT * FROM sos_records ORDER BY triggeredAt DESC")
    fun getAllRecords(): Flow<List<SosRecordEntity>>

    @Query("SELECT * FROM sos_records WHERE id = :id")
    suspend fun getById(id: String): SosRecordEntity?

    @Query("SELECT * FROM sos_records WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveRecord(): Flow<SosRecordEntity?>

    @Query("UPDATE sos_records SET status = :status, cancelledAt = :cancelledAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, cancelledAt: Long? = null)

    @Query("UPDATE sos_records SET syncedToFirestore = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT * FROM sos_records WHERE syncedToFirestore = 0")
    suspend fun getUnsyncedRecords(): List<SosRecordEntity>

    @Query("SELECT COUNT(*) FROM sos_records")
    fun getTotalSosCount(): Flow<Int>

    @Query("DELETE FROM sos_records WHERE triggeredAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
