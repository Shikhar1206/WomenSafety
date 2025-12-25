package com.example.womensafety.fakecall

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.womensafety.R

class FakeCallActivity : AppCompatActivity() {

    private lateinit var player: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_call)

        player = MediaPlayer.create(this, R.raw.ringtone)
        player.start()

        findViewById<View>(R.id.btnAnswer).setOnClickListener {
            findViewById<TextView>(R.id.tvIncoming).text = "Call in progress"
            player.stop()
        }

        findViewById<View>(R.id.btnEndCall).setOnClickListener {
            finish()
        }

    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}