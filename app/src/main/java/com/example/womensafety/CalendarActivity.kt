package com.example.womensafety.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.womensafety.databinding.ActivityCalendarBinding

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    private var lastClickTime = 0L
    private var lastDayClicked = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            val currentTime = System.currentTimeMillis()

            // 13
            if (dayOfMonth == 13 &&
                dayOfMonth == lastDayClicked &&
                currentTime - lastClickTime < 500
            ) {
                openMainActivity()
            }

            lastDayClicked = dayOfMonth
            lastClickTime = currentTime
        }
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
