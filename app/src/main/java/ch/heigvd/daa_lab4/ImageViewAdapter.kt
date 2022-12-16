package ch.heigvd.daa_lab4

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import java.net.URL

class ImageViewAdapter(
    private val items: List<URL>,
    private val imageLoader: ImageLoader
) : RecyclerView.Adapter<ImageViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = R.layout.image_grid_item
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView by lazy { view.findViewById<ImageView>(R.id.image_view) }
        private val progressBar by lazy { view.findViewById<ProgressBar>(R.id.progress_bar) }
        private var job: Job? = null

        fun bind(imageUrl: URL) {
            job = imageLoader.load(imageUrl, ::showImage)
        }

        fun unbind() {
            imageView.setImageBitmap(null)
            imageView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            job?.cancel()
        }

        private fun showImage(bitmap: Bitmap) {
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}