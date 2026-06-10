package com.example.womensafety.data.local.dao

import androidx.room.*
import com.example.womensafety.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(contact: ContactEntity)

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM contacts WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveContactsSnapshot(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE syncedToFirestore = 0")
    suspend fun getUnsyncedContacts(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE phone = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): ContactEntity?

    @Query("SELECT COUNT(*) FROM contacts WHERE isActive = 1")
    fun getActiveContactCount(): Flow<Int>

    @Query("UPDATE contacts SET syncedToFirestore = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE contacts SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: String)


}
