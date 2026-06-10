package com.example.womensafety.util

import android.content.Context
import android.telephony.SmsManager
import timber.log.Timber

/**
 * Fixed: Added missing package declaration.
 * Fixed: Replaced deprecated SmsManager.getDefault() with context.getSystemService().
 * Fixed: Added divideMessage for messages > 160 chars.
 */

object SmsUtil {

    fun sendSms(context: Context, phone: String, message: String) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
            Timber.d("SMS sent to $phone")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send SMS to $phone")
        }
    }

    fun sendSmsToAll(context: Context, phones: List<String>, message: String) {
        phones.forEach { phone -> sendSms(context, phone, message) }
    }
}
