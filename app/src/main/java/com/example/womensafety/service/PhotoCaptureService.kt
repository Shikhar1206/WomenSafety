package com.example.womensafety.service

import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.os.*
import android.provider.MediaStore
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.womensafety.R
import java.io.File

class PhotoCaptureService : LifecycleService() {

    private lateinit var imageCapture: ImageCapture
    private val handler = Handler(Looper.getMainLooper())
    private val useFrontCamera = false


    private val captureRunnable = object : Runnable {
        override fun run() {
            if (::imageCapture.isInitialized) {
                capturePhoto()
            }
            handler.postDelayed(this, 10 * 1000L) // Can be anything like for 30 seconds replace with 30*1000LL
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(101, buildNotification())
        startCamera()
//        handler.post(captureRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(captureRunnable)
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

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/WomenSafety"
            )
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Photo saved to Gallery successfully
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun buildNotification(): Notification {
        val channelId = "photo_capture_channel"

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
            .setContentTitle("WomenSafety")
            .setContentText("Evidence capture active")
            .setSmallIcon(R.drawable.ic_camera)
            .build()
    }
}
