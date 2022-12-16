package ch.heigvd.daa_lab4

import android.os.Bundle
import android.view.Menu
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageLoader = ImageLoader(lifecycleScope, cacheDir, CACHE_DURATION)
        val images = (1..UPPER_BOUND).map { URL(IMAGE_URL.format(it)) }

        val recyclerView = findViewById<RecyclerView>(R.id.image_list)
        recyclerView.adapter = ImageViewAdapter(images, imageLoader)
        recyclerView.layoutManager = GridLayoutManager(this, NB_COLUMNS)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}