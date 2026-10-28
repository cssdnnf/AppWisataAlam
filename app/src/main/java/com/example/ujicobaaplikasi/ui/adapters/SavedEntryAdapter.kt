package com.example.ujicobaaplikasi.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.ImageTools
import com.example.ujicobaaplikasi.data.SavedType
import com.google.android.material.card.MaterialCardView

data class SavedEntry(
    val type: SavedType,
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val imageRes: Int,
    val imageBytes: ByteArray?,
    val rating: String,
    val reviewCount: String,
    val price: String,
)

class SavedEntryAdapter(
    private val onOpen: (SavedEntry) -> Unit,
    private val onRemove: (SavedEntry) -> Unit,
) : RecyclerView.Adapter<SavedEntryAdapter.SavedViewHolder>() {

    private var items: List<SavedEntry> = emptyList()

    fun submitList(newItems: List<SavedEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destination_compact, parent, false)
        return SavedViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class SavedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val image = itemView.findViewById<ImageView>(R.id.compactImage)
        private val category = itemView.findViewById<TextView>(R.id.compactCategory)
        private val title = itemView.findViewById<TextView>(R.id.compactTitle)
        private val location = itemView.findViewById<TextView>(R.id.compactLocation)
        private val rating = itemView.findViewById<TextView>(R.id.compactRating)
        private val reviews = itemView.findViewById<TextView>(R.id.compactReviews)
        private val price = itemView.findViewById<TextView>(R.id.compactPrice)
        private val favorite = itemView.findViewById<AppCompatImageButton>(R.id.compactFavorite)

        fun bind(entry: SavedEntry) {
            val context = itemView.context
            ImageTools.bind(
                image,
                entry.imageRes,
                entry.imageBytes,
                "saved-${entry.type.name}-${entry.id}",
            )
            image.contentDescription = entry.title
            category.text = entry.category.ifBlank { context.getString(entry.type.labelRes) }
            title.text = entry.title
            location.text = entry.subtitle
            rating.text = entry.rating.ifBlank { "—" }
            reviews.text = context.getString(R.string.format_reviews, entry.reviewCount)
            price.text = entry.price.ifBlank { "—" }
            favorite.setImageResource(R.drawable.ic_heart_filled)
            favorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.koral),
            )
            favorite.contentDescription = context.getString(R.string.cd_unfavorite)
            favorite.setOnClickListener { onRemove(entry) }
            card.contentDescription = entry.title
            card.setOnClickListener { onOpen(entry) }
        }
    }
}
