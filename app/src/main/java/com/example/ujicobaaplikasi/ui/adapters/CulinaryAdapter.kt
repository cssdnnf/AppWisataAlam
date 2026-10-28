package com.example.ujicobaaplikasi.ui.adapters

import android.view.LayoutInflater
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.ImageTools
import com.example.ujicobaaplikasi.data.Culinary
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class CulinaryAdapter(
    private val onOpen: (Culinary) -> Unit,
    private val onFavoriteToggle: (Culinary) -> Unit = {},
) : RecyclerView.Adapter<CulinaryAdapter.CulinaryViewHolder>() {

    private var items: List<Culinary> = emptyList()
    private var favorites: Set<String> = emptySet()

    fun submitList(newItems: List<Culinary>, newFavorites: Set<String> = emptySet()) {
        items = newItems
        favorites = newFavorites
        notifyDataSetChanged()
    }

    fun updateFavorites(newFavorites: Set<String>) {
        favorites = newFavorites
        if (itemCount > 0) notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CulinaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_culinary, parent, false)
        return CulinaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CulinaryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CulinaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val image = itemView.findViewById<ImageView>(R.id.culinaryImage)
        private val category = itemView.findViewById<TextView>(R.id.culinaryCategory)
        private val rating = itemView.findViewById<TextView>(R.id.culinaryRating)
        private val name = itemView.findViewById<TextView>(R.id.culinaryName)
        private val origin = itemView.findViewById<TextView>(R.id.culinaryOrigin)
        private val price = itemView.findViewById<TextView>(R.id.culinaryPrice)
        private val favorite = itemView.findViewById<AppCompatImageButton>(R.id.culinaryFavorite)

        fun bind(culinary: Culinary) {
            val context = itemView.context
            val saved = culinary.id in favorites
            ImageTools.bind(image, culinary.imageRes, culinary.imageBytes, "cul-${culinary.id}")
            image.contentDescription = culinary.name
            category.text = culinary.category.uppercase(Locale("id", "ID"))
            rating.text = culinary.rating.toString()
            name.text = culinary.name
            origin.text = culinary.origin
            price.text = culinary.price
            favorite.setImageResource(
                if (saved) R.drawable.ic_heart_filled else R.drawable.ic_heart,
            )
            favorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (saved) R.color.koral else R.color.teks_sekunder,
                ),
            )
            favorite.contentDescription = context.getString(
                if (saved) R.string.cd_unfavorite else R.string.cd_favorite,
            )
            favorite.setOnClickListener { onFavoriteToggle(culinary) }
            card.setOnClickListener { onOpen(culinary) }
        }
    }
}
