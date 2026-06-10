package com.example.womensafety.presentation.fakecall

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.womensafety.R
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import com.example.womensafety.databinding.ActivityFakeCallBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * FakeCallActivity — simulated incoming emergency call screen.
 * Uses Hilt to load dynamic caller name and number from Jetpack DataStore.
 */

@AndroidEntryPoint
class FakeCallActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    private lateinit var binding: ActivityFakeCallBinding
    private var player: MediaPlayer? = null
    private var callTimer: CountDownTimer? = null
    private var isCallAnswered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFakeCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set stream volume for ringtone playback
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        try {
            audioManager.setStreamVolume(
                AudioManager.STREAM_RING,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
                0
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to maximize ring volume")
        }

        // Play custom mock ringtone from raw resources
        try {
            player = MediaPlayer.create(this, R.raw.ringtone)
            player?.isLooping = true
            player?.start()
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize or play ringtone audio")
        }

        // Fetch dynamic caller name and number from preferences
        lifecycleScope.launch {
            try {
                val prefs = preferencesDataStore.userPreferences.first()
                binding.tvCallerName.text = prefs.fakeCallName
                binding.tvCallerNumber.text = prefs.fakeCallNumber
            } catch (e: Exception) {
                Timber.e(e, "Failed to load caller details from preferences, using defaults")
                binding.tvCallerName.text = "Mom"
                binding.tvCallerNumber.text = "+91 94128 93028"
            }
        }

        binding.btnAnswer.setOnClickListener {
            if (!isCallAnswered) {
                isCallAnswered = true
                answerCall()
            }
        }

        binding.btnEndCall.setOnClickListener {
            finish()
        }

        // Auto-answer/dismiss if user doesn't pick up within 25 seconds
        callTimer = object : CountDownTimer(25_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (!isCallAnswered) finish()
            }
        }.start()
    }

    private fun answerCall() {
        try {
            player?.stop()
            player?.release()
        } catch (e: Exception) {
            Timber.e(e, "Error stopping ringtone player")
        }
        player = null

        binding.tvIncoming.text = "Call in progress..."
        binding.btnAnswer.isEnabled = false

        // Automatically hang up after 60 seconds of call
        callTimer?.cancel()
        callTimer = object : CountDownTimer(60_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() { finish() }
        }.start()
    }

    override fun onDestroy() {
        callTimer?.cancel()
        try {
            player?.stop()
            player?.release()
        } catch (e: Exception) {
            // Ignored
        }
        player = null
        super.onDestroy()
    }
}
