package com.example.womensafety.data.repository

import android.content.Context
import android.telephony.SmsManager
import com.example.womensafety.core.util.Constants
import com.example.womensafety.core.util.Resource
import com.example.womensafety.data.local.dao.ContactDao
import com.example.womensafety.data.local.dao.SosRecordDao
import com.example.womensafety.data.local.entity.SosRecordEntity
import com.example.womensafety.domain.model.SosRecord
import com.example.womensafety.domain.repository.SosRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import com.example.womensafety.service.LocationTrackingService
import com.example.womensafety.service.PhotoCaptureService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sosRecordDao: SosRecordDao,
    private val contactDao: ContactDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val preferencesDataStore: UserPreferencesDataStore
) : SosRepository {

    override suspend fun triggerSos(
        triggeredBy: String,
        lat: Double?,
        lng: Double?,
        address: String?
    ): Resource<String> {
        return try {
            val sosId = UUID.randomUUID().toString()
            val locationStr = if (lat != null && lng != null)
                "https://maps.google.com/?q=$lat,$lng"
            else "Location unavailable"

            // Save to Room
            val record = SosRecordEntity(
                id = sosId,
                triggeredBy = triggeredBy,
                latitude = lat,
                longitude = lng,
                locationAddress = address
            )
            sosRecordDao.insert(record)

            // Send SMS to all active contacts
            val contacts = contactDao.getActiveContactsSnapshot()
            if (contacts.isNotEmpty()) {
                val smsManager = context.getSystemService(SmsManager::class.java)
                val message = buildSosMessage(locationStr)
                val parts = smsManager.divideMessage(message)
                var sentCount = 0
                contacts.forEach { contact ->
                    try {
                        smsManager.sendMultipartTextMessage(
                            contact.phone, null, parts, null, null
                        )
                        sentCount++
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to send SMS to ${contact.name}")
                    }
                }
                sosRecordDao.update(record.copy(smsSentCount = sentCount))
            }

            // Start LocationTrackingService in background
            try {
                val trackingIntent = Intent(context, LocationTrackingService::class.java).apply {
                    putExtra(LocationTrackingService.EXTRA_SOS_ID, sosId)
                }
                ContextCompat.startForegroundService(context, trackingIntent)
            } catch (e: Exception) {
                Timber.e(e, "Failed to start LocationTrackingService from repository")
            }

            // Start PhotoCaptureService in background if enabled and camera permission is granted
            try {
                val prefs = preferencesDataStore.userPreferences.first()
                if (prefs.autoCaptureEnabled) {
                    val hasCamera = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasCamera) {
                        val cameraIntent = Intent(context, PhotoCaptureService::class.java)
                        ContextCompat.startForegroundService(context, cameraIntent)
                    } else {
                        Timber.w("Camera permission not granted. Skipping photo capture service start.")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start PhotoCaptureService from repository")
            }

            syncToFirestore()
            Resource.Success(sosId)
        } catch (e: Exception) {
            Timber.e(e, "SOS trigger failed")
            Resource.Error("SOS failed: ${e.message}", e)
        }
    }

    override suspend fun cancelSos(sosId: String): Resource<Unit> {
        return try {
            sosRecordDao.updateStatus(
                sosId,
                Constants.SOS_STATUS_CANCELLED,
                System.currentTimeMillis()
            )

            // Stop background tracking and camera evidence services
            try {
                context.stopService(Intent(context, LocationTrackingService::class.java))
                context.stopService(Intent(context, PhotoCaptureService::class.java))
            } catch (e: Exception) {
                Timber.e(e, "Failed to stop services on SOS cancel")
            }

            syncToFirestore()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to cancel SOS: ${e.message}", e)
        }
    }

    override fun getAllSosRecords(): Flow<List<SosRecord>> =
        sosRecordDao.getAllRecords().map { records -> records.map { it.toDomain() } }

    override fun getActiveSos(): Flow<SosRecord?> =
        sosRecordDao.getActiveRecord().map { it?.toDomain() }

    override suspend fun syncToFirestore() {
        try {
            val userId = auth.currentUser?.uid
            val unsynced = sosRecordDao.getUnsyncedRecords()
            unsynced.forEach { record ->
                val map = record.toFirestoreMap().toMutableMap().apply {
                    if (userId != null) {
                        put("userId", userId)
                    }
                }
                firestore.collection("sos_records")
                    .document(record.id)
                    .set(map)
                    .await()
                sosRecordDao.markAsSynced(record.id)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync SOS records")
        }
    }

    private fun buildSosMessage(locationLink: String): String =
        "🚨 EMERGENCY SOS!\n" +
        "I am in danger and need immediate help!\n" +
        "📍 My Location:\n$locationLink\n" +
        "— Sent via Smartify Safety App"

    private fun SosRecordEntity.toDomain() = SosRecord(
        id = id,
        triggeredAt = triggeredAt,
        triggeredBy = triggeredBy,
        latitude = latitude,
        longitude = longitude,
        locationAddress = locationAddress,
        smsSentCount = smsSentCount,
        status = status,
        cancelledAt = cancelledAt
    )

    private fun SosRecordEntity.toFirestoreMap() = mapOf(
        "id" to id,
        "triggeredAt" to triggeredAt,
        "triggeredBy" to triggeredBy,
        "latitude" to latitude,
        "longitude" to longitude,
        "smsSentCount" to smsSentCount,
        "status" to status
    )
}
