package ch.heigvd.daa_lab4

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.work.*
import java.io.File
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Permet de gérer un cache d'images dans un dossier donné.
 * Fournit également le nécessaire pour nettoyer le cache périodiquement ou à la demande.
 *
 * @author Marengo Stéphane, Friedli Jonathan, Silvestri Géraud
 */
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

    /**
     * Enregistre une tâche périodique de nettoyage du cache.
     * Aucun effet si une tâche périodique est déjà enregistrée.
     *
     * @param cleanInterval intervalle entre chaque nettoyage, doit être supérieur à [MIN_INTERVAL] minutes
     * @param context le contexte de l'application
     */
    fun registerPeriodicCleanup(cleanInterval: Duration, context: Context) {
        if (cleanInterval < MIN_INTERVAL) {
            throw IllegalArgumentException("interval cannot be smaller than ${MIN_INTERVAL.toMinutes()} minutes")
        }

        val workManager = WorkManager.getInstance(context)
        val tag = PERIODIC_TAG + directory.name

        val workRequest =
            PeriodicWorkRequestBuilder<DirectoryCleanerWorker>(cleanInterval)
                .addTag(tag)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(DirectoryCleanerWorker.createInputData(directory))
                .build()

        workManager.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }

    /**
     * Vide le cache.
     *
     * @param context le contexte de l'application
     */
    fun cleanup(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val workRequest = OneTimeWorkRequestBuilder<DirectoryCleanerWorker>()
            .addTag(ONE_TIME_TAG)
            .setInputData(DirectoryCleanerWorker.createInputData(directory))
            .build()

        workManager.enqueue(workRequest)
    }

    /**
     * Retourne l'image associée à la clé donnée si elle existe dans le cache et est valide.
     *
     * @param key la clé de l'image
     * @param expirationTime la durée pendant laquelle l'image est considérée valide
     * @return l'image (sous forme de bytes) associée à la clé donnée si elle existe et est valide, null sinon
     */
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

    /**
     * Ajoute l'image associée à la clé donnée au cache.
     *
     * @param key la clé de l'image
     * @param bitmap l'image à ajouter au cache
     */
    fun put(key: String, bitmap: Bitmap) {
        val file = File(directory, key)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    }
}