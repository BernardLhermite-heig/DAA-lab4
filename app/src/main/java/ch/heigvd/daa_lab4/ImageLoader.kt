package ch.heigvd.daa_lab4

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.time.Duration

/**
 * Type du callback utilisé lorsque le chargement d'une image est terminé.
 */
typealias Callback = (Bitmap) -> Unit

/**
 * Gère le chargement et la mise en cache d'image depuis une URL de manière asynchrone.
 *
 * @author Marengo Stéphane, Friedli Jonathan, Silvestri Géraud
 */
class ImageLoader(
    private val lifecycleScope: CoroutineScope,
    private val cacheManager: CacheManager,
    private val maxCacheDuration: Duration
) {
    /**
     * Chargée l'image à partir de l'URL donnée et appelle le callback lorsque le chargement est terminé.
     * Après téléchargement, l'image est mise en cache.
     *
     * @param imageURL URL de l'image à charger
     * @param onComplete Callback à appeler lorsque le chargement est terminé
     */
    fun load(imageURL: URL, onComplete: Callback): Job {
        return lifecycleScope.launch {
            if (tryLoadFromCache(imageURL, onComplete)) {
                return@launch
            }

            downloadImage(imageURL)?.let { bytes ->
                decodeImage(bytes)?.let { bitmap ->
                    putInCache(imageURL, bitmap)
                    completeLoading(bitmap, onComplete)
                }
            }
        }
    }

    /**
     * Tente de charger l'image depuis le cache. Si l'opération réussit, le callback est appelé.
     *
     * @param imageURL URL de l'image à charger
     * @param onComplete Callback à appeler lorsque le chargement est terminé
     * @return true si l'image a été chargée depuis le cache, false sinon
     */
    private suspend fun tryLoadFromCache(imageURL: URL, onComplete: Callback): Boolean {
        return getFromCache(imageURL)?.let { bytes ->
            decodeImage(bytes)?.let { bitmap ->
                completeLoading(bitmap, onComplete)
                true
            }
        } ?: false
    }

    /**
     * Appel le callback dans le thread principal.
     */
    private suspend fun completeLoading(bitmap: Bitmap, onComplete: Callback) =
        withContext(Dispatchers.Main) {
            onComplete(bitmap)
        }

    /**
     * Télécharge l'image à partir de l'URL donnée et retourne les bytes téléchargés.
     *
     * @param url l'URL de l'image à télécharger
     * @return les bytes téléchargés ou null si une erreur est survenue
     */
    private suspend fun downloadImage(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        try {
            url.readBytes()
        } catch (e: IOException) {
            Log.e("downloadImage", "Exception while downloading image ${e.message}", e)
            null
        }
    }

    /**
     * Décode les bytes donnés en bitmap.
     *
     * @param bytes les bytes à décoder
     * @return le bitmap décodé ou null si une erreur est survenue
     */
    private suspend fun decodeImage(bytes: ByteArray): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: IOException) {
            Log.e("decodeImage", "Exception while decoding image ${e.message}", e)
            null
        }
    }

    /**
     * Met l'image en cache.
     *
     * @param url l'URL de l'image à mettre en cache (utilisée comme clé)
     * @param bitmap l'image à mettre en cache
     */
    private suspend fun putInCache(url: URL, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        cacheManager.put(url.hashCode().toString(), bitmap)
    }

    /**
     * Retourne l'image associée à l'URL donnée depuis le cache si elle existe.
     *
     * @param url l'URL de l'image à charger depuis le cache
     * @return les bytes de l'image ou null si l'image n'est pas présente dans le cache
     */
    private suspend fun getFromCache(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        cacheManager.get(url.hashCode().toString(), maxCacheDuration)
    }
}