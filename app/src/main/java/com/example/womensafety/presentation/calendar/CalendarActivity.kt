package com.example.womensafety.presentation.calendar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import com.example.womensafety.databinding.ActivityCalendarBinding
import com.example.womensafety.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CalendarActivity — disguise launcher screen.
 * Shows a calendar. Double-tapping the configured day reveals the main app.
 */

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    private lateinit var binding: ActivityCalendarBinding

    private var lastClickTime = 0L
    private var lastDayClicked = -1
    private var targetUnlockDay = 13 // Default to 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load custom unlock day from preferences
        lifecycleScope.launch {
            try {
                val prefs = preferencesDataStore.userPreferences.first()
                targetUnlockDay = prefs.calendarUnlockDay
            } catch (e: Exception) {
                // Fallback to default
            }
        }

        binding.calendarView.setOnDateChangeListener { _, _, _, dayOfMonth ->
            val currentTime = System.currentTimeMillis()

            if (dayOfMonth == targetUnlockDay) {
                // First tap on target day: record it
                // Second tap on target day within 800ms: unlock
                if (lastDayClicked == targetUnlockDay && currentTime - lastClickTime < 800) {
                    openMainApp()
                    return@setOnDateChangeListener
                }
            }

            lastDayClicked = dayOfMonth
            lastClickTime = currentTime
        }
    }

    private fun openMainApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
