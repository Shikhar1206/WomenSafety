package com.example.womensafety.util

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import timber.log.Timber

/**
 * Fixed: Global singleton MediaPlayer can leak if Activity is destroyed.
 * Fixed: Added null-check before stop to prevent IllegalStateException.
 * Usage: Provide a lifecycle-aware owner or ensure stopSiren() is called in onDestroy.
 */

object SirenUtil {

    private var mediaPlayer: MediaPlayer? = null

    fun startSiren(context: Context) {
        if (mediaPlayer?.isPlaying == true) return

        try {
            val audioManager = context.applicationContext
                .getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Max volume on MUSIC stream
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )

            mediaPlayer = MediaPlayer.create(context.applicationContext, com.example.womensafety.R.raw.siren)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            Timber.d("Siren started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start siren")
        }
    }

    fun stopSiren() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop siren")
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}
