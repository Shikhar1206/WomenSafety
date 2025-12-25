import android.content.Context
import android.telephony.SmsManager
import com.example.womensafety.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//package com.example.womensafety.util
//
//import android.content.Context
//import android.telephony.SmsManager
//import android.util.Log
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//object SmsUtil {
//    fun sendSOS(context: Context, location: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val contacts = AppDatabase
//                .getDatabase(context)
//                .contactDao()
//                .getAll()
//
//            if (contacts.isEmpty()) {
//                Log.e("SMS", "No contacts found")
//                return@launch
//            }
//
//            val message =
//                "EMERGENCY! EMERGENCY!\nI am in danger.\nLocation:\n$location"
//
//            val smsManager = SmsManager.getDefault()
//            val parts = smsManager.divideMessage(message)
//
//            contacts.forEach {
//                smsManager.sendMultipartTextMessage(
//                    it.phone,
//                    null,
//                    parts,
//                    null,
//                    null
//                )
//            }
//        }
//    }
//}
//


object SmsUtil {

    fun sendSOS(context: Context, location: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val appContext = context.applicationContext

            val contacts = AppDatabase
                .getDatabase(appContext)
                .contactDao()
                .getAll()

            if (contacts.isEmpty()) return@launch

            val smsManager = SmsManager.getDefault()
            val message =
                "🚨 EMERGENCY!\nI am in danger.\nLocation:\n$location"

            val parts = smsManager.divideMessage(message)

            contacts.forEach {
                smsManager.sendMultipartTextMessage(
                    it.phone,
                    null,
                    parts,
                    null,
                    null
                )
            }
        }
    }
}
