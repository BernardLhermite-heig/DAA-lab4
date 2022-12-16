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
    private val cacheDir: File,
    private val maxCacheDuration: Duration
) {
    init {
        if (!cacheDir.exists() || !cacheDir.isDirectory) {
            throw IllegalArgumentException("cacheDir must be a directory")
        }
    }

    fun load(imageURL: URL, showImage: (Bitmap) -> Unit): Job {
        return lifecycleScope.launch {
            val image = getCachedImage(imageURL)

            val bytes = if (image != null) {
                readImageFile(image)
            } else {
                downloadImage(imageURL)
            }
            val bitmap = decodeImage(bytes) ?: return@launch
            cacheImage(imageURL, bitmap)
            displayImage(showImage, bitmap)
        }
    }

    private suspend fun downloadImage(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        try {
            url.readBytes()
        } catch (e: IOException) {
            Log.w("downloadImage", "Exception while downloading image", e)
            null
        }
    }

    private suspend fun readImageFile(file: File): ByteArray? = withContext(Dispatchers.IO) {
        try {
            file.readBytes()
        } catch (e: IOException) {
            Log.w("readImageFile", "Exception while reading image file", e)
            null
        }
    }

    private suspend fun decodeImage(bytes: ByteArray?): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes?.size ?: 0)
        } catch (e: IOException) {
            Log.w("decodeImage", "Exception while decoding image", e)
            null
        }
    }

    private suspend fun cacheImage(url: URL, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val file = File(cacheDir, url.hashCode().toString())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    }

    private suspend fun getCachedImage(url: URL): File? = withContext(Dispatchers.IO) {
        val file = File(cacheDir, url.hashCode().toString())

        if (file.exists() && file.lastModified() + maxCacheDuration.toMillis() > System.currentTimeMillis()) {
            file
        } else {
            file.delete()
            null
        }
    }

    private suspend fun displayImage(showImage: (Bitmap) -> Unit, bmp: Bitmap): Unit =
        withContext(Dispatchers.Main) {
            showImage(bmp)
            Log.i("displayImage", "Image displayed")
        }
}