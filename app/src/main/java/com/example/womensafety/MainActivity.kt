package com.example.womensafety.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.womensafety.contacts.ContactsActivity
import com.example.womensafety.databinding.ActivityMainBinding
import com.example.womensafety.service.EmergencyService
import com.example.womensafety.fakecall.FakeCallActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts
        .RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()

        binding.btnSOS.setOnClickListener {
            ContextCompat.startForegroundService(
                this,
                Intent(this, EmergencyService::class.java)
            )
        }

        binding.btnFakeCall.setOnClickListener {
            startActivity(
                Intent(this, FakeCallActivity::class.java)
            )
        }

        binding.btnContacts.setOnClickListener {
            startActivity(
                Intent(this, ContactsActivity::class.java)
            )
        }

    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val denied = permissions.any { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (denied) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

}

