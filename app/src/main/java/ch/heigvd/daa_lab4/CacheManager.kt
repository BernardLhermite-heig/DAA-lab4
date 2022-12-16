package ch.heigvd.daa_lab4

import android.content.Context
import androidx.work.*
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

class CacheManager(private val directory: File, private val cleanInterval: Duration) {
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

        if (cleanInterval.isNegative || cleanInterval.isZero || cleanInterval < MIN_INTERVAL) {
            throw IllegalArgumentException("cleanInterval must be greater than 15min")
        }
    }

    fun register(context: Context) {
        if (hasRegistered) {
            throw IllegalStateException("CacheManager has already been registered")
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

    fun clean(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val workRequest = OneTimeWorkRequestBuilder<DirectoryCleanerWorker>()
            .setInputData(DirectoryCleanerWorker.createInputData(directory))
            .build()

        workManager.enqueue(workRequest)
    }
}