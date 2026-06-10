package com.example.womensafety.core.util

object Constants {
    // Notification Channels
    const val CHANNEL_EMERGENCY = "channel_emergency"
    const val CHANNEL_PHOTO_CAPTURE = "channel_photo_capture"
    const val CHANNEL_SAFE_WORD = "channel_safe_word"
    const val CHANNEL_LOCATION = "channel_location"
    const val CHANNEL_GENERAL = "channel_general"

    // Notification IDs (all unique)
    const val NOTIF_ID_EMERGENCY_SERVICE = 1001
    const val NOTIF_ID_PHOTO_CAPTURE = 1002
    const val NOTIF_ID_SAFE_WORD = 1003
    const val NOTIF_ID_LOCATION_SERVICE = 1004
    const val NOTIF_ID_SOS_ACTIVE = 1005
    const val NOTIF_ID_BATTERY_ALERT = 1006

    // SOS
    const val SOS_COUNTDOWN_SECONDS = 10
    const val SOS_TRIGGER_MANUAL = "MANUAL"
    const val SOS_TRIGGER_SHAKE = "SHAKE"
    const val SOS_TRIGGER_VOICE = "VOICE"
    const val SOS_TRIGGER_BATTERY = "BATTERY"

    const val SOS_STATUS_ACTIVE = "ACTIVE"
    const val SOS_STATUS_CANCELLED = "CANCELLED"
    const val SOS_STATUS_RESOLVED = "RESOLVED"

    // Shake detector
    const val SHAKE_THRESHOLD = 13.0f
    const val SHAKE_COOLDOWN_MS = 1500L

    // Safe Word defaults
    val SAFE_WORDS = listOf("help", "emergency", "save me", "bachao", "help me")

    // Battery threshold
    const val BATTERY_CRITICAL_PERCENT = 15

    // DataStore keys
    const val PREFS_SOS_ENABLED = "sos_enabled"
    const val PREFS_SHAKE_ENABLED = "shake_enabled"
    const val PREFS_SHAKE_SENSITIVITY = "shake_sensitivity"
    const val PREFS_SAFE_WORD_ENABLED = "safe_word_enabled"
    const val PREFS_SAFE_WORD = "safe_word"
    const val PREFS_FAKE_CALL_NAME = "fake_call_name"
    const val PREFS_FAKE_CALL_NUMBER = "fake_call_number"
    const val PREFS_AUTO_CAPTURE_ENABLED = "auto_capture_enabled"
    const val PREFS_BATTERY_SOS_ENABLED = "battery_sos_enabled"
    const val PREFS_USER_LOGGED_IN = "user_logged_in"
    const val PREFS_CALENDAR_UNLOCK_ENABLED = "calendar_unlock_enabled"
    const val PREFS_CALENDAR_UNLOCK_DAY = "calendar_unlock_day"

    // Broadcast Actions
    const val ACTION_CANCEL_SOS = "com.example.womensafety.CANCEL_SOS"

    // WorkManager tags
    const val WORK_TAG_SYNC_CONTACTS = "sync_contacts"
    const val WORK_TAG_SYNC_SOS = "sync_sos"
    const val WORK_TAG_CHECK_IN = "check_in"

    // FCM Topics
    const val FCM_TOPIC_SOS = "sos_alerts"
}
