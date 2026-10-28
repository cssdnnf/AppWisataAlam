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
import com.example.ujicobaaplikasi.data.Destination
import com.google.android.material.card.MaterialCardView

class FeaturedDestinationAdapter(
    private val onOpen: (Destination) -> Unit,
    private val onFavoriteToggle: (Destination) -> Unit,
) : RecyclerView.Adapter<FeaturedDestinationAdapter.FeaturedViewHolder>() {

    private var items: List<Destination> = emptyList()
    private var favorites: Set<String> = emptySet()

    fun submitList(newItems: List<Destination>, newFavorites: Set<String>) {
        items = newItems
        favorites = newFavorites
        notifyDataSetChanged()
    }

    fun updateFavorites(newFavorites: Set<String>) {
        favorites = newFavorites
        if (itemCount > 0) notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destination_featured, parent, false)
        return FeaturedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FeaturedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val image = itemView.findViewById<ImageView>(R.id.featuredImage)
        private val category = itemView.findViewById<TextView>(R.id.featuredCategory)
        private val rating = itemView.findViewById<TextView>(R.id.featuredRating)
        private val reviews = itemView.findViewById<TextView>(R.id.featuredReviews)
        private val title = itemView.findViewById<TextView>(R.id.featuredTitle)
        private val location = itemView.findViewById<TextView>(R.id.featuredLocation)
        private val favorite = itemView.findViewById<AppCompatImageButton>(R.id.featuredFavorite)
        private val price = itemView.findViewById<TextView>(R.id.featuredPrice)
        private val duration = itemView.findViewById<TextView>(R.id.featuredDuration)

        fun bind(destination: Destination) {
            val context = itemView.context
            val favoriteState = destination.id in favorites
            ImageTools.bind(image, destination.imageRes, destination.imageBytes, "featured-${destination.id}")
            image.contentDescription = destination.name
            category.text = destination.category
            rating.text = destination.rating.toString()
            reviews.text = context.getString(R.string.format_reviews, destination.reviewCount)
            title.text = destination.name
            location.text = destination.location
            price.text = if (destination.price.equals("Gratis", ignoreCase = true)) {
                destination.price
            } else {
                context.getString(R.string.format_start_from, destination.price)
            }
            duration.text = destination.duration

            favorite.setImageResource(
                if (favoriteState) R.drawable.ic_heart_filled else R.drawable.ic_heart,
            )
            favorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (favoriteState) R.color.koral else R.color.teks_utama,
                ),
            )
            favorite.contentDescription = context.getString(
                if (favoriteState) R.string.cd_unfavorite else R.string.cd_favorite,
            )
            favorite.setOnClickListener { onFavoriteToggle(destination) }
            card.setOnClickListener { onOpen(destination) }
        }
    }
}
