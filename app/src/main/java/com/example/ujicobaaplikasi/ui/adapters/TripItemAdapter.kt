package com.example.ujicobaaplikasi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.TripItem

class TripItemAdapter(
    private val onOpen: (TripItem) -> Unit,
    private val onDelete: (TripItem) -> Unit,
) : RecyclerView.Adapter<TripItemAdapter.ItemViewHolder>() {

    private var items: List<TripItem> = emptyList()

    fun submitList(newItems: List<TripItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_detail, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val day = itemView.findViewById<TextView>(R.id.tripItemDay)
        private val time = itemView.findViewById<TextView>(R.id.tripItemTime)
        private val title = itemView.findViewById<TextView>(R.id.tripDetailItemTitle)
        private val notes = itemView.findViewById<TextView>(R.id.tripDetailItemNotes)
        private val delete = itemView.findViewById<View>(R.id.tripDetailItemDelete)

        fun bind(item: TripItem) {
            val context = itemView.context
            day.text = context.getString(R.string.trip_day_badge, item.dayNumber)
            time.text = item.timeLabel
            time.visibility = if (item.timeLabel.isBlank()) View.GONE else View.VISIBLE
            title.text = item.title
            notes.text = item.notes
            notes.visibility = if (item.notes.isBlank()) View.GONE else View.VISIBLE
            itemView.contentDescription = item.title
            itemView.setOnClickListener { onOpen(item) }
            delete.setOnClickListener { onDelete(item) }
        }
    }
}
