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
import com.example.ujicobaaplikasi.data.MenuEntry
import com.google.android.material.card.MaterialCardView

class MenuAdapter(
    private val onMenuClick: (MenuEntry) -> Unit,
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var items: List<MenuEntry> = emptyList()

    fun submitList(newItems: List<MenuEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val icon = itemView.findViewById<ImageView>(R.id.menuIcon)
        private val title = itemView.findViewById<TextView>(R.id.menuTitle)
        private val subtitle = itemView.findViewById<TextView>(R.id.menuSubtitle)
        private val chevron = itemView.findViewById<ImageView>(R.id.menuChevron)

        fun bind(menu: MenuEntry) {
            val context = itemView.context
            val accent = ContextCompat.getColor(context, menu.iconTintRes)
            card.setCardBackgroundColor(ContextCompat.getColor(context, menu.containerColorRes))
            icon.setImageResource(menu.iconRes)
            icon.imageTintList = ColorStateList.valueOf(accent)
            chevron.imageTintList = ColorStateList.valueOf(accent)
            title.setText(menu.titleRes)
            subtitle.setText(menu.subtitleRes)
            itemView.contentDescription = context.getString(menu.titleRes)
            card.setOnClickListener { onMenuClick(menu) }
        }
    }
}
