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
import com.example.ujicobaaplikasi.data.Experience
import com.example.ujicobaaplikasi.data.ImageTools
import com.google.android.material.card.MaterialCardView

class ExperienceAdapter(
    private val onOpen: (Experience) -> Unit,
    private val onFavoriteToggle: (Experience) -> Unit,
) : RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder>() {

    private var items: List<Experience> = emptyList()
    private var favorites: Set<String> = emptySet()

    fun submitList(newItems: List<Experience>, newFavorites: Set<String>) {
        items = newItems
        favorites = newFavorites
        notifyDataSetChanged()
    }

    fun updateFavorites(newFavorites: Set<String>) {
        favorites = newFavorites
        if (itemCount > 0) notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destination_compact, parent, false)
        return ExperienceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ExperienceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val image = itemView.findViewById<ImageView>(R.id.compactImage)
        private val category = itemView.findViewById<TextView>(R.id.compactCategory)
        private val title = itemView.findViewById<TextView>(R.id.compactTitle)
        private val location = itemView.findViewById<TextView>(R.id.compactLocation)
        private val rating = itemView.findViewById<TextView>(R.id.compactRating)
        private val reviews = itemView.findViewById<TextView>(R.id.compactReviews)
        private val price = itemView.findViewById<TextView>(R.id.compactPrice)
        private val favorite = itemView.findViewById<AppCompatImageButton>(R.id.compactFavorite)

        fun bind(experience: Experience) {
            val context = itemView.context
            val favoriteState = experience.id in favorites
            ImageTools.bind(
                image,
                experience.imageRes,
                experience.imageBytes,
                "exp-${experience.type.name}-${experience.id}",
            )
            image.contentDescription = experience.title
            category.text = experience.category.ifBlank {
                context.getString(experience.type.labelRes)
            }
            title.text = experience.title
            location.text = experience.location.ifBlank { experience.subtitle }
            rating.text = if (experience.rating > 0) experience.rating.toString() else "—"
            reviews.text = context.getString(R.string.format_reviews, experience.reviewCount)
            price.text = experience.price.ifBlank { experience.badge }
            favorite.setImageResource(
                if (favoriteState) R.drawable.ic_heart_filled else R.drawable.ic_heart,
            )
            favorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (favoriteState) R.color.koral else R.color.teks_sekunder,
                ),
            )
            favorite.contentDescription = context.getString(
                if (favoriteState) R.string.cd_unfavorite else R.string.cd_favorite,
            )
            favorite.setOnClickListener { onFavoriteToggle(experience) }
            card.setOnClickListener { onOpen(experience) }
        }
    }
}
