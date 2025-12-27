package com.example.womensafety.util

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.womensafety.R

object SirenUtil {

    private var mediaPlayer: MediaPlayer? = null

    fun startSiren(context: Context) {
        if (mediaPlayer != null) return

        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Force max volume
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )

        mediaPlayer = MediaPlayer.create(context, R.raw.siren)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun stopSiren() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}
