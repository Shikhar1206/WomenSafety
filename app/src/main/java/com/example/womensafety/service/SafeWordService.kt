package com.example.womensafety.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import androidx.core.app.NotificationCompat
import com.example.womensafety.R
//import com.example.womensafety.util.SmsUtil
import java.util.Locale

class SafeWordService : Service() {

    private val SAFE_WORD = "kotlin"
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(101, createNotification())
        startListening()
    }

    private fun startListening() {

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle) {
                val matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                matches?.forEach {
                    if (it.lowercase().contains(SAFE_WORD)) {
                        triggerSOS()
                    }
                }
                restartListening()
            }

            override fun onError(error: Int) {
                restartListening()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
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
        speechRecognizer?.stopListening()
        startListening()
    }

    private fun triggerSOS() {
//        SmsUtil.sendSOS("Safe word triggered")
        callEmergencyNumber()
    }

    private fun callEmergencyNumber() {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:112")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(callIntent)
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
        speechRecognizer?.destroy()
        speechRecognizer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
