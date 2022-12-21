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

/**
 * Adapter chargé de gérer la liste d'images à afficher
 *
 * @author Marengo Stéphane, Friedli Jonathan, Silvestri Géraud
 */
class ImageViewAdapter(
    private val items: List<URL>,
    private val imageLoader: ImageLoader
) : RecyclerView.Adapter<ImageViewAdapter.ViewHolder>() {

    /**
     * Force le rechargement des images
     */
    fun reload() {
        notifyDataSetChanged()
    }

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

        /**
         * Charge l'image de manière asynchrone à partir de l'URL donnée.
         */
        fun bind(imageUrl: URL) {
            job = imageLoader.load(imageUrl, ::showImage)
        }

        /**
         * Réaffiche le spinner de progression et annule l'éventuel chargement d'image en cours.
         */
        fun unbind() {
            imageView.setImageBitmap(null)
            imageView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            job?.cancel()
        }

        /**
         * Affiche l'image donnée dans l'ImageView.
         */
        private fun showImage(bitmap: Bitmap) {
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}