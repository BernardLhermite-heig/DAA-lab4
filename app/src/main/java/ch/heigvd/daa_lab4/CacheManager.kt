package ch.heigvd.daa_lab4

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.work.*
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

class CacheManager(private val directory: File) {
    companion object {
        private val MIN_INTERVAL = Duration.ofMinutes(15)
    }

    private var hasRegistered = false

    init {
        if (!directory.exists() || !directory.isDirectory) {
            throw IllegalArgumentException("$directory must be a directory")
        }

        if (!directory.canWrite()) {
            throw IllegalArgumentException("$directory must be writable")
        }
    }

    fun registerPeriodicCleanup(cleanInterval: Duration, context: Context) {
        if (hasRegistered) {
            Log.i(CacheManager::class.java.name, "Periodic cleanup already registered, skipping")
            return
        }

        if (cleanInterval < MIN_INTERVAL) {
            throw IllegalArgumentException("interval cannot be smaller than ${MIN_INTERVAL.toMinutes()} minutes")
        }

        val workManager = WorkManager.getInstance(context)

        val workRequest =
            PeriodicWorkRequestBuilder<DirectoryCleanerWorker>(cleanInterval)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(DirectoryCleanerWorker.createInputData(directory))
                .build()

        workManager.enqueue(workRequest)
        hasRegistered = true
    }

    fun cleanup(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val workRequest = OneTimeWorkRequestBuilder<DirectoryCleanerWorker>()
            .setInputData(DirectoryCleanerWorker.createInputData(directory))
            .build()

        workManager.enqueue(workRequest)
    }

    fun get(key: String): File? {
        val file = File(directory, key)
        return if (file.exists()) file else null
    }

    fun put(key: String, bitmap: Bitmap) {
        val file = File(directory, key)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    }
}