package com.example.womensafety.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.postDelayed
import com.example.womensafety.R
//import com.example.womensafety.util.SmsUtil
import java.util.Locale
//import java.util.logging.Handler
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import android.Manifest

class SafeWordService : Service() {

//    private val SAFE_WORD = "Hello"
    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isListening = false
    private val LISTEN_DURATION =  10*60*1000L // 10 minutes

    override fun onCreate() {
        super.onCreate()
        startForeground(101, createNotification())
        startListening()
        handler.postDelayed({
            stopSelf()
        }, LISTEN_DURATION)
    }

    private fun startListening() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SafeWord", "Listening started")
            }

            override fun onResults(results: Bundle) {
                val matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                var safeWordDetected = false

                matches?.forEach {
                    val spoken = it.lowercase()

                    if (
                        spoken.contains("help") ||
                        spoken.contains("emergency") ||
                        spoken.contains("save me")
                    ) {
                        safeWordDetected = true
                        triggerSOS()
                    }
                }

                if (safeWordDetected) {
                    stopSelf()
                } else {
                    restartListening() // non-safe word → restart listening
                }
            }

            override fun onError(error: Int) {
                Log.e("SafeWord", "Error code: $error")
                restartListening()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun restartListening() {
        handler.postDelayed({
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {}

            startListening()
        }, 1000) // 1 second gap
    }

    private fun createNotification(): Notification {
        val channelId = "SAFE_WORD_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Safe Word Listening",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("WomenSafety Active")
            .setContentText("Listening for safe word")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    override fun onDestroy() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
        super.onDestroy()
    }

    private fun triggerSOS() {

        Log.d("SafeWord", "Triggering SOS")

        SmsUtil.sendSOS(
            applicationContext,
            "Safe word activated"
        )

        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:112")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(callIntent)
        } else {
            Log.e("SafeWord", "CALL_PHONE permission not granted")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
