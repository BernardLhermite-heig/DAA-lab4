package ch.heigvd.daa_lab4

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.time.Duration

typealias Callback = (Bitmap) -> Unit

class ImageLoader(
    private val lifecycleScope: CoroutineScope,
    private val cacheManager: CacheManager,
    private val maxCacheDuration: Duration
) {
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

    private suspend fun tryLoadFromCache(imageURL: URL, onComplete: Callback): Boolean {
        return getFromCache(imageURL)?.let { bytes ->
            decodeImage(bytes)?.let { bitmap ->
                completeLoading(bitmap, onComplete)
                true
            }
        } ?: false
    }

    private suspend fun completeLoading(bitmap: Bitmap, onComplete: Callback) =
        withContext(Dispatchers.Main) {
            onComplete(bitmap)
        }

    private suspend fun downloadImage(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        try {
            url.readBytes()
        } catch (e: IOException) {
            Log.e("downloadImage", "Exception while downloading image ${e.message}", e)
            null
        }
    }

    private suspend fun decodeImage(bytes: ByteArray): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: IOException) {
            Log.e("decodeImage", "Exception while decoding image ${e.message}", e)
            null
        }
    }

    private suspend fun putInCache(url: URL, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        cacheManager.put(url.hashCode().toString(), bitmap)
    }

    private suspend fun getFromCache(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        cacheManager.get(url.hashCode().toString(), maxCacheDuration)
    }
}