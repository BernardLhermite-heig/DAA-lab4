package ch.heigvd.daa_lab4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImageViewAdapter(items: List<String> = listOf()) : RecyclerView.Adapter<ImageViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = R.layout.recyclerview_image_layout
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.myImageView.setImageResource(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val myImageView: Any = view.findViewById(R.id.image_view)
    }
}