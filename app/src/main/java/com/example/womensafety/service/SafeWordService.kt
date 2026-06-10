package com.example.womensafety.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.womensafety.core.util.Constants
import com.example.womensafety.receiver.SosActionReceiver
import com.example.womensafety.domain.usecase.sos.TriggerSosUseCase
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

/**
 * Background service that uses speech recognition to monitor for emergency keywords/safe-words.
 */

@AndroidEntryPoint
class SafeWordService : LifecycleService() {

    @Inject
    lateinit var triggerSosUseCase: TriggerSosUseCase

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var restartCount = 0
    private val maxRestarts = 20  // ~20 minutes of listening with 1s gaps
    private val listenDurationMs = 10 * 60 * 1000L  // 10 minutes max then auto-stop

    // Safe words to detect (configurable via DataStore in future)
    private val safeWords = listOf("help", "emergency", "save me", "bachao", "help me")
    private var customSafeWord = "help"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        
        serviceScope.launch {
            try {
                preferencesDataStore.userPreferences.collect { prefs ->
                    customSafeWord = prefs.safeWord
                    Timber.d("SafeWordService preference updated: customSafeWord=$customSafeWord")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error collecting safe word preferences")
            }
        }
        
        startListening()

        // Auto-stop after 10 minutes
        handler.postDelayed({ stopSelf() }, listenDurationMs)
        Timber.d("SafeWordService started")
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Timber.w("Speech recognition not available on this device")
            stopSelf()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Timber.d("SafeWord: listening started (attempt $restartCount)")
            }

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val wordToCheck = customSafeWord.lowercase()
                val detected = matches?.any { spoken ->
                    val spokenLower = spoken.lowercase()
                    spokenLower.contains(wordToCheck) || 
                    safeWords.any { word -> spokenLower.contains(word) }
                } == true

                if (detected) {
                    Timber.d("SafeWord detected in results! Triggering SOS.")
                    triggerSos()
                    stopSelf()
                } else {
                    scheduleRestart()
                }
            }

            override fun onError(error: Int) {
                Timber.e("SafeWord recognition error code: $error")
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        // Reset restart count because silence/timeout is normal behavior
                        restartCount = 0
                        scheduleRestart()
                    }
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_SERVER -> {
                        restartCount = 0
                        Timber.w("Network/Server error — pausing safe word service")
                        handler.postDelayed({ scheduleRestart() }, 5000L)
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        handler.postDelayed({ scheduleRestart() }, 2000L)
                    }
                    else -> {
                        scheduleRestart()
                    }
                }
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val wordToCheck = customSafeWord.lowercase()
                val detected = matches?.any { spoken ->
                    val spokenLower = spoken.lowercase()
                    spokenLower.contains(wordToCheck) || 
                    safeWords.any { word -> spokenLower.contains(word) }
                } == true

                if (detected) {
                    Timber.d("SafeWord detected in partial results! Triggering SOS.")
                    triggerSos()
                    stopSelf()
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun scheduleRestart() {
        if (restartCount >= maxRestarts) {
            Timber.w("SafeWord max restart limit reached — stopping service")
            stopSelf()
            return
        }
        restartCount++
        handler.postDelayed({
            destroyRecognizer()
            startListening()
        }, 1000L)
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Timber.e(e, "Error destroying SpeechRecognizer")
        } finally {
            speechRecognizer = null
        }
    }

    private fun triggerSos() {
        // Broadcast to SosActionReceiver which delegates to the SOS use case
        val intent = Intent(this, SosActionReceiver::class.java).apply {
            action = "com.example.womensafety.VOICE_SOS"
        }
        sendBroadcast(intent)

        serviceScope.launch {
            val location = com.example.womensafety.util.LocationHelper.getCurrentLocationSuspend(this@SafeWordService)
            val lat = location?.first
            val lng = location?.second
            triggerSosUseCase(
                triggeredBy = Constants.SOS_TRIGGER_VOICE,
                lat = lat,
                lng = lng
            )
        }
    }

    private fun startForegroundNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            Constants.CHANNEL_SAFE_WORD,
            "Safe Word Listening",
            NotificationManager.IMPORTANCE_LOW  // Fixed: was HIGH (intrusive)
        ).apply {
            description = "Listening for safe word in background"
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_SAFE_WORD)
            .setContentTitle(getString(com.example.womensafety.R.string.notif_safe_word_title))
            .setContentText(getString(com.example.womensafety.R.string.notif_safe_word_text))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setSilent(true)
            .build()

        startForeground(
            Constants.NOTIF_ID_SAFE_WORD,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        handler.removeCallbacksAndMessages(null)  // Fixed: prevent handler leak
        destroyRecognizer()
        Timber.d("SafeWordService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)
}
