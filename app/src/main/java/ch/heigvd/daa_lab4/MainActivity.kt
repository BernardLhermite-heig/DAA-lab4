package ch.heigvd.daa_lab4

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.URL
import java.time.Duration

class MainActivity : AppCompatActivity() {
    companion object {
        const val NB_COLUMNS = 3
        const val IMAGE_URL = "https://daa.iict.ch/images/%d.jpg"
        const val UPPER_BOUND = 10_000
        val CACHE_DURATION: Duration = Duration.ofMinutes(5)
        val CACHE_CLEAN_INTERVAL: Duration = Duration.ofMinutes(15)
    }

    private val cacheManager by lazy { CacheManager(cacheDir) }
    private lateinit var adapter: ImageViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageLoader = ImageLoader(lifecycleScope, cacheManager, CACHE_DURATION)
        val images = (1..UPPER_BOUND).map { URL(IMAGE_URL.format(it)) }

        adapter = ImageViewAdapter(images, imageLoader)
        
        val recyclerView = findViewById<RecyclerView>(R.id.image_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, NB_COLUMNS)

        cacheManager.registerPeriodicCleanup(CACHE_CLEAN_INTERVAL, this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main_menu_cleanup_button -> {
                cacheManager.cleanup(this)
                adapter.reload()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}