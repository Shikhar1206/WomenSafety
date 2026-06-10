package com.example.womensafety.service

import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.os.*
import android.provider.MediaStore
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.womensafety.core.util.Constants
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PhotoCaptureService : LifecycleService() {

    @Inject
    lateinit var storage: FirebaseStorage

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    private lateinit var imageCapture: ImageCapture
    private val handler = Handler(Looper.getMainLooper())
    private val useFrontCamera = false

    private val captureRunnable = object : Runnable {
        override fun run() {
            lifecycleScope.launch {
                try {
                    val prefs = preferencesDataStore.userPreferences.first()
                    if (prefs.autoCaptureEnabled) {
                        if (::imageCapture.isInitialized) {
                            capturePhoto()
                        }
                    } else {
                        Timber.d("Auto photo capture disabled by user preferences. Stopping service.")
                        stopSelf()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error checking preferences in runnable")
                }
            }
            handler.postDelayed(this, 10 * 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()

        startForeground(
            Constants.NOTIF_ID_PHOTO_CAPTURE,
            buildNotification(),
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        )

        startCamera()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = if (useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                imageCapture
            )
            handler.post(captureRunnable)

        }, ContextCompat.getMainExecutor(this))
    }


    private fun capturePhoto() {
        try {
            val outputDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
            val photoFile = File(
                outputDirectory,
                "evidence_${System.currentTimeMillis()}.jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val uri = android.net.Uri.fromFile(photoFile)
                        Timber.d("Evidence photo saved locally: $uri")
                        
                        try {
                            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            mediaScanIntent.data = uri
                            sendBroadcast(mediaScanIntent)
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to broadcast media scanner intent")
                        }

                        uploadPhotoToFirebase(uri)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Timber.e(exception, "Failed to capture evidence photo")
                    }
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error during photo capture setup")
        }
    }

    private fun uploadPhotoToFirebase(uri: android.net.Uri) {
        val uid = auth.currentUser?.uid ?: "anonymous"
        val ref = storage.reference.child("evidence/$uid/IMG_${System.currentTimeMillis()}.jpg")
        ref.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                Timber.d("Evidence photo uploaded to Firebase: ${taskSnapshot.metadata?.path}")
            }
            .addOnFailureListener { e ->
                Timber.w(e, "Failed to upload photo to Firebase Storage (using Spark plan - local file preserved)")
            }
    }

    private fun buildNotification(): Notification {
        val channelId = Constants.CHANNEL_PHOTO_CAPTURE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Evidence Capture",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(com.example.womensafety.R.string.notif_photo_title))
            .setContentText(getString(com.example.womensafety.R.string.notif_photo_text))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setSilent(true)
            .build()
    }
}
