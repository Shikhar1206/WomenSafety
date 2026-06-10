package com.example.womensafety.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.womensafety.core.util.Constants
import com.example.womensafety.domain.repository.SosRepository
import com.example.womensafety.domain.usecase.sos.CancelSosUseCase
import com.example.womensafety.domain.usecase.sos.TriggerSosUseCase
import com.example.womensafety.service.EmergencyService
import com.example.womensafety.util.LocationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

/**
 * Handles SOS-related broadcast actions:
 * - Cancel SOS notification action button
 * - Shake-triggered SOS from EmergencyService
 * - Battery-critical SOS from BatteryReceiver
 * Uses Hilt for dependency injection and handles tasks asynchronously using goAsync().
 */
@AndroidEntryPoint
class SosActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var triggerSosUseCase: TriggerSosUseCase

    @Inject
    lateinit var cancelSosUseCase: CancelSosUseCase

    @Inject
    lateinit var sosRepository: SosRepository

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("SosActionReceiver received action: ${intent.action}")

        val pendingResult = goAsync()
        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        coroutineScope.launch {
            try {
                when (intent.action) {
                    Constants.ACTION_CANCEL_SOS -> {
                        Timber.d("ACTION_CANCEL_SOS received. Stopping EmergencyService and cancelling SOS record in DB.")
                        // Stop emergency service
                        context.stopService(Intent(context, EmergencyService::class.java))
                        
                        // Cancel the active SOS record in the database
                        val activeSos = sosRepository.getActiveSos().first()
                        if (activeSos != null) {
                            cancelSosUseCase(activeSos.id)
                            Timber.d("Successfully cancelled active SOS: ${activeSos.id}")
                        } else {
                            Timber.w("No active SOS found to cancel in database")
                        }
                    }
                    "com.example.womensafety.BATTERY_SOS" -> {
                        Timber.d("BATTERY_SOS received. Fetching location and triggering SOS database record & SMS dispatch.")
                        val location = LocationHelper.getCurrentLocationSuspend(context)
                        val lat = location?.first
                        val lng = location?.second
                        val result = triggerSosUseCase(
                            triggeredBy = Constants.SOS_TRIGGER_BATTERY,
                            lat = lat,
                            lng = lng
                        )
                        Timber.d("Battery SOS trigger result: $result")
                    }
                    "com.example.womensafety.SHAKE_SOS" -> {
                        Timber.d("SHAKE_SOS received (EmergencyService has initiated database trigger and contact SMS).")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling broadcast in SosActionReceiver")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
