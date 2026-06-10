package com.example.womensafety.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.womensafety.domain.repository.ContactRepository
import com.example.womensafety.domain.repository.SosRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * WorkManager CoroutineWorker — runs background sync to Firestore.
 * Triggered after every contact add/edit and SOS event.
 * Automatically retries on network failure.
 */

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contactRepository: ContactRepository,
    private val sosRepository: SosRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("SyncWorker: starting sync")
            contactRepository.syncToFirestore()
            sosRepository.syncToFirestore()
            Timber.d("SyncWorker: sync complete")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker: sync failed")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
