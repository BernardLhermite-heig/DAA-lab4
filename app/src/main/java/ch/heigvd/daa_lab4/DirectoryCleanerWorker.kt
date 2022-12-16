package ch.heigvd.daa_lab4

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class DirectoryCleanerWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {
    companion object {
        const val DIRECTORY_KEY = "DIRECTORY_KEY"

        fun createInputData(directory: File): Data {
            if (!directory.exists() || !directory.isDirectory) {
                throw IllegalArgumentException("$directory is not a directory")
            }

            if (!directory.canWrite()) {
                throw IllegalArgumentException("$directory is not writable")
            }

            return Data.Builder()
                .putString(DIRECTORY_KEY, directory.absolutePath)
                .build()
        }

    }

    override fun doWork(): Result {
        val pathStr = inputData.getString(DIRECTORY_KEY)
        val directory = pathStr?.let { File(it) }

        if (directory == null || !directory.isDirectory) {
            return Result.failure()
        }

        if (!directory.canWrite()) {
            return Result.failure()
        }

        val result = directory.listFiles()?.fold(true) { result, file ->
            result && file.deleteRecursively()
        }

        return if (result != null && result) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}