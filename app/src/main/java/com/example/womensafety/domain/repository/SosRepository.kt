package com.example.womensafety.domain.repository

import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.model.SosRecord
import kotlinx.coroutines.flow.Flow

interface SosRepository {
    suspend fun triggerSos(triggeredBy: String, lat: Double?, lng: Double?, address: String?): Resource<String>
    suspend fun cancelSos(sosId: String): Resource<Unit>
    fun getAllSosRecords(): Flow<List<SosRecord>>
    fun getActiveSos(): Flow<SosRecord?>
    suspend fun syncToFirestore()
}
