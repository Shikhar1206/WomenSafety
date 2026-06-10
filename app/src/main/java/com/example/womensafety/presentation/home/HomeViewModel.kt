package com.example.womensafety.presentation.home

import android.content.Context
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.womensafety.core.util.Constants
import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.repository.ContactRepository
import com.example.womensafety.domain.repository.SosRepository
import com.example.womensafety.domain.usecase.sos.CancelSosUseCase
import com.example.womensafety.domain.usecase.sos.TriggerSosUseCase
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import com.example.womensafety.data.preferences.UserPreferences
import com.example.womensafety.service.EmergencyService
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject


sealed class SosUiState {
    object Idle : SosUiState()
    data class Countdown(val secondsLeft: Int, val sosId: String = "") : SosUiState()
    object Active : SosUiState()
    data class Error(val message: String) : SosUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val triggerSosUseCase: TriggerSosUseCase,
    private val cancelSosUseCase: CancelSosUseCase,
    private val contactRepository: ContactRepository,
    private val sosRepository: SosRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _sosState = MutableStateFlow<SosUiState>(SosUiState.Idle)
    val sosState: StateFlow<SosUiState> = _sosState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = userPreferencesDataStore.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val contactCount = contactRepository.getContactCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private var countdownJob: Job? = null
    private var pendingSosId: String = ""
    private var isAutoCaptureEnabled = false

    init {
        viewModelScope.launch {
            userPreferencesDataStore.userPreferences.collect { prefs ->
                isAutoCaptureEnabled = prefs.autoCaptureEnabled

                // Manage EmergencyService (Shake Detector)
                if (prefs.shakeEnabled) {
                    startEmergencyService()
                } else {
                    stopEmergencyService()
                }

                // Manage SafeWordService (Voice Activator)
                if (prefs.safeWordEnabled) {
                    startSafeWordService()
                } else {
                    stopSafeWordService()
                }

                // Manage PhotoCaptureService (Auto Photo Capture) in real-time
                if (isAutoCaptureEnabled) {
                    startPhotoCaptureService()
                } else {
                    stopPhotoCaptureService()
                }
            }
        }

        viewModelScope.launch {
            sosRepository.getActiveSos().collect { activeSos ->
                if (activeSos != null) {
                    pendingSosId = activeSos.id
                    _sosState.value = SosUiState.Active
                    startLocationTrackingService(activeSos.id)
                    if (isAutoCaptureEnabled) {
                        startPhotoCaptureService()
                    }
                } else {
                    if (_sosState.value is SosUiState.Active) {
                        _sosState.value = SosUiState.Idle
                        pendingSosId = ""
                        stopLocationTrackingService()
                        stopPhotoCaptureService()
                    }
                }
            }
        }
    }

    /** Called when user taps SOS button — starts 10-second countdown */
    fun startSosCountdown() {
        if (_sosState.value is SosUiState.Countdown || _sosState.value is SosUiState.Active) return
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (seconds in Constants.SOS_COUNTDOWN_SECONDS downTo 1) {
                _sosState.value = SosUiState.Countdown(seconds)
                delay(1000L)
            }
            // Countdown finished — trigger SOS
            fireSos(Constants.SOS_TRIGGER_MANUAL)
        }
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Cancel the countdown before it fires */
    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _sosState.value = SosUiState.Idle
    }

    /** Trigger SOS immediately (from shake/voice) */
    fun fireSosImmediately(trigger: String) {
        cancelCountdown()
        viewModelScope.launch { fireSos(trigger) }
    }

    private suspend fun fireSos(trigger: String) {
        // Get last known location
        val (lat, lng) = getLastLocation()

        if (!hasSmsPermission()) {
            _sosState.value = SosUiState.Error("SMS permission required for SOS")
            return
        }
        when (val result = triggerSosUseCase(trigger, lat, lng)) {
            is Resource.Success -> {
                pendingSosId = result.data
                _sosState.value = SosUiState.Active
            }
            is Resource.Error -> {
                _sosState.value = SosUiState.Error(result.message)
                Timber.e("SOS trigger failed: ${result.message}")
            }
            is Resource.Loading -> Unit
        }
    }

    fun cancelActiveSos() {
        viewModelScope.launch {
            if (pendingSosId.isNotEmpty()) {
                cancelSosUseCase(pendingSosId)
            }
            _sosState.value = SosUiState.Idle
            pendingSosId = ""
        }
    }

    fun dismissError() {
        _sosState.value = SosUiState.Idle
    }

    private fun startEmergencyService() {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (hasLocation) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, EmergencyService::class.java)
            )
        } else {
            Timber.w("Cannot start EmergencyService — location permission not granted")
            _sosState.value = SosUiState.Error("Location permission required for SOS")
        }
    }

    private fun stopEmergencyService() {
        context.stopService(Intent(context, EmergencyService::class.java))
    }

    private fun startLocationTrackingService(sosId: String) {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasLocation) {
            val intent = Intent(context, com.example.womensafety.service.LocationTrackingService::class.java).apply {
                putExtra(com.example.womensafety.service.LocationTrackingService.EXTRA_SOS_ID, sosId)
            }
            context.startForegroundService(intent)
        }
    }

    private fun stopLocationTrackingService() {
        context.stopService(Intent(context, com.example.womensafety.service.LocationTrackingService::class.java))
    }

    private fun startPhotoCaptureService() {
        val hasCamera = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasCamera) {
            val intent = Intent(context, com.example.womensafety.service.PhotoCaptureService::class.java)
            context.startForegroundService(intent)
        }
    }

    private fun stopPhotoCaptureService() {
        context.stopService(Intent(context, com.example.womensafety.service.PhotoCaptureService::class.java))
    }

    private fun startSafeWordService() {
        val hasMic = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasMic) {
            val intent = Intent(context, com.example.womensafety.service.SafeWordService::class.java)
            context.startForegroundService(intent)
        } else {
            Timber.w("Cannot start SafeWordService — microphone permission not granted")
        }
    }

    private fun stopSafeWordService() {
        context.stopService(Intent(context, com.example.womensafety.service.SafeWordService::class.java))
    }

    private suspend fun getLastLocation(): Pair<Double?, Double?> {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocation) {
            Timber.w("Location permission not granted. Skipping getLastLocation.")
            return Pair(null, null)
        }

        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location = client.lastLocation.await()
            Pair(location?.latitude, location?.longitude)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get location")
            Pair(null, null)
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }
}
