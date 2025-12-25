package com.example.womensafety.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDao {
    @Insert
    suspend fun insert(contact: EmergencyContactEntity)

    @Query("SELECT * FROM emergency_contacts")
    suspend fun getAll(): List<EmergencyContactEntity>

//    @Query("DELETE FROM emergency_contacts")
//    suspend fun deleteAll()
}