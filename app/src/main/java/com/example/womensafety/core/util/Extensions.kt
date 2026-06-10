package com.example.womensafety.core.util

import android.content.Context
import android.telephony.PhoneNumberUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


fun String.isValidPhoneNumber(): Boolean {
    val cleaned = this.replace(Regex("[^0-9+]"), "")
    return cleaned.length in 7..15 && (cleaned.startsWith("+") || cleaned.all { it.isDigit() })
}

fun String.isValidName(): Boolean = this.trim().length >= 2

// Timestamp
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        else -> this.toFormattedDate()
    }
}

// Context utilities
fun Context.dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

// Google Maps location link
fun locationToMapsLink(lat: Double, lng: Double): String =
    "https://maps.google.com/?q=$lat,$lng"
