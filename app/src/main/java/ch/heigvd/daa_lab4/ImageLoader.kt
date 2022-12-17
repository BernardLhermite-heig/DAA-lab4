package ch.heigvd.daa_lab4

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.net.URL
import java.time.Duration

class ImageLoader(
    private val lifecycleScope: CoroutineScope,
    private val cacheManager: CacheManager,
    private val maxCacheDuration: Duration
) {
    fun load(imageURL: URL, onComplete: (Bitmap) -> Unit): Job {
        return lifecycleScope.launch {
            val image = getCachedImage(imageURL)

            val bytes = if (image != null) {
                readImageFile(image)
            } else {
                downloadImage(imageURL)
            } ?: return@launch

            val bitmap = decodeImage(bytes) ?: return@launch
            cacheImage(imageURL, bitmap)

            withContext(Dispatchers.Main) {
                onComplete(bitmap)
            }
        }
    }

    private suspend fun downloadImage(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        try {
            url.readBytes()
        } catch (e: IOException) {
            Log.e("downloadImage", "Exception while downloading image ${e.message}", e)
            null
        }
    }

    private suspend fun readImageFile(file: File): ByteArray? = withContext(Dispatchers.IO) {
        try {
            file.readBytes()
        } catch (e: IOException) {
            Log.e("readImageFile", "Exception while reading image file ${e.message}", e)
            null
        }
    }

    private suspend fun decodeImage(bytes: ByteArray?): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes?.size ?: 0)
        } catch (e: IOException) {
            Log.e("decodeImage", "Exception while decoding image ${e.message}", e)
            null
        }
    }

    private suspend fun cacheImage(url: URL, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        cacheManager.put(url.hashCode().toString(), bitmap)
    }

    private suspend fun getCachedImage(url: URL): File? = withContext(Dispatchers.IO) {
        val file = cacheManager.get(url.hashCode().toString())

        if (file != null && file.lastModified() + maxCacheDuration.toMillis() > System.currentTimeMillis()) {
            file
        } else {
            null
        }
    }
}