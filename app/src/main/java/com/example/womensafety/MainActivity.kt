package com.example.womensafety.ui

import android.Manifest
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log.d
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.womensafety.contacts.ContactsActivity
import com.example.womensafety.databinding.ActivityMainBinding
import com.example.womensafety.service.EmergencyService
import com.example.womensafety.fakecall.FakeCallActivity
import com.example.womensafety.service.PhotoCaptureService
import com.example.womensafety.service.SafeWordService
import com.example.womensafety.util.SirenUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts
        .RequestMultiplePermissions()) {}

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startPhotoCaptureService()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission required for auto capture",
                    Toast.LENGTH_SHORT
                ).show()
                binding.switchAutoCapture.isChecked = false
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()

        binding.btnSOS.setOnClickListener {
//            ContextCompat.startForegroundService(
//                this,
//                Intent(this, EmergencyService::class.java)
//            )
//            startActivity(
//                Intent(this, FakeCallActivity::class.java)
//            )
//            binding.switchAutoCapture.setOnCheckedChangeListener { _, isChecked ->
//                if (isChecked) {
//                    checkAndStartCamera()
//                } else {
//                    stopPhotoCaptureService()
//                }
//            }
            activateSOS()
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

//        ContextCompat.startForegroundService(
//            this,
//            Intent(this, EmergencyService::class.java)
//        )
//        binding.btnStartService.setOnClickListener {
//            ContextCompat.startForegroundService(
//                this,
//                Intent(this, EmergencyService::class.java)
//            )
//        }

        binding.switchAutoCapture.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkAndStartCamera()
            } else {
                stopPhotoCaptureService()
            }
        }

        binding.btnStartService.setOnClickListener{
            ContextCompat.startForegroundService(
                this,
                Intent(this, EmergencyService::class.java)
            )
        }

        binding.btnSiren.setOnClickListener {
            if (SirenUtil.isPlaying()) {
                SirenUtil.stopSiren()
                Toast.makeText(this, "Siren stopped", Toast.LENGTH_SHORT).show()
            } else {
                SirenUtil.startSiren(this)
                Toast.makeText(this, "Emergency siren activated", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSafeWord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startSafeWordService()
            } else {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            binding.btnSafeWord.backgroundTintList =
                ContextCompat.getColorStateList(
                    this,
                    android.R.color.holo_green_dark
                )

        }



    }

    private fun startSafeWordService() {
        val intent = Intent(this, SafeWordService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Safe word listening enabled", Toast.LENGTH_SHORT).show()
    }

    private val audioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startSafeWordService()
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

    private fun checkAndStartCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startPhotoCaptureService()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startPhotoCaptureService() {
        val intent = Intent(this, PhotoCaptureService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        Toast.makeText(
            this,
            "Auto photo capture enabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun stopPhotoCaptureService() {
        stopService(Intent(this, PhotoCaptureService::class.java))

        Toast.makeText(
            this,
            "Auto photo capture disabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun activateSOS() {

        SirenUtil.startSiren(this)

        ContextCompat.startForegroundService(
            this,
            Intent(this, EmergencyService::class.java)
        )

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startPhotoCaptureService()
            binding.switchAutoCapture.isChecked = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

//        startActivity(Intent(this, FakeCallActivity::class.java))

        Toast.makeText(this, "SOS Activated", Toast.LENGTH_LONG).show()
    }




}

