package ch.heigvd.daa_lab4

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.work.*
import java.io.File
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeUnit

class CacheManager(private val directory: File) {
    companion object {
        private val MIN_INTERVAL = Duration.ofMinutes(15)
        private const val PERIODIC_TAG = "PeriodicCleanup"
        private const val ONE_TIME_TAG = "OneTimeCleanup"
    }

    init {
        if (!directory.exists() || !directory.isDirectory) {
            throw IllegalArgumentException("$directory must be a directory")
        }

        if (!directory.canWrite()) {
            throw IllegalArgumentException("$directory must be writable")
        }
    }

    fun registerPeriodicCleanup(cleanInterval: Duration, context: Context) {
        if (cleanInterval < MIN_INTERVAL) {
            throw IllegalArgumentException("interval cannot be smaller than ${MIN_INTERVAL.toMinutes()} minutes")
        }

        val workManager = WorkManager.getInstance(context)

        val workRequest =
            PeriodicWorkRequestBuilder<DirectoryCleanerWorker>(cleanInterval)
                .addTag(PERIODIC_TAG)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(DirectoryCleanerWorker.createInputData(directory))
                .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cleanup(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val workRequest = OneTimeWorkRequestBuilder<DirectoryCleanerWorker>()
            .addTag(ONE_TIME_TAG)
            .setInputData(DirectoryCleanerWorker.createInputData(directory))
            .build()

        workManager.enqueue(workRequest)
    }

    fun get(key: String, expirationTime: Duration): ByteArray? {
        val file = File(directory, key)
        if (!file.exists() || file.lastModified() + expirationTime.toMillis() <= System.currentTimeMillis()) {
            return null
        }

        return try {
            file.readBytes()
        } catch (e: IOException) {
            Log.e("readImageFile", "Exception while reading image file ${e.message}", e)
            null
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        val file = File(directory, key)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    }
}