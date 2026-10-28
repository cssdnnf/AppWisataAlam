package com.example.ujicobaaplikasi.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.ImageTools
import com.google.android.material.card.MaterialCardView

data class ImageChoice(
    val key: String,
    val label: String,
    val sourceLabel: String,
    val drawableRes: Int? = null,
    val thumbnail: ByteArray? = null,
)

class ImageChoiceAdapter(
    private var selectedKey: String,
    private val onSelect: (ImageChoice) -> Unit,
) : RecyclerView.Adapter<ImageChoiceAdapter.ChoiceViewHolder>() {

    private var items: List<ImageChoice> = emptyList()

    fun submitList(newItems: List<ImageChoice>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateSelection(key: String) {
        selectedKey = key
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_choice, parent, false)
        return ChoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChoiceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val preview = itemView.findViewById<ImageView>(R.id.imageChoicePreview)
        private val label = itemView.findViewById<TextView>(R.id.imageChoiceLabel)
        private val source = itemView.findViewById<TextView>(R.id.imageChoiceSource)

        fun bind(choice: ImageChoice) {
            val context = itemView.context
            val selected = choice.key == selectedKey
            if (choice.thumbnail != null) {
                ImageTools.bindThumbnail(preview, choice.thumbnail, R.drawable.bg_lombok)
            } else {
                preview.setImageResource(choice.drawableRes ?: R.drawable.bg_lombok)
            }
            label.text = choice.label
            source.text = choice.sourceLabel
            card.strokeWidth = if (selected) {
                (2 * context.resources.displayMetrics.density).toInt()
            } else {
                (1 * context.resources.displayMetrics.density).toInt()
            }
            card.strokeColor = ContextCompat.getColor(
                context,
                if (selected) R.color.hijau_lombok else R.color.garis_lembut,
            )
            card.cardElevation = if (selected) 2f else 0f
            card.contentDescription = choice.label
            card.setOnClickListener { onSelect(choice) }
        }
    }
}
