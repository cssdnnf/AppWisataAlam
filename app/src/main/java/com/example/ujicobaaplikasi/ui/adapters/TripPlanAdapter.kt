package com.example.ujicobaaplikasi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.TripPlan
import com.example.ujicobaaplikasi.ui.trip.TripDateFormatter

class TripPlanAdapter(
    private val onOpen: (TripPlan) -> Unit,
) : RecyclerView.Adapter<TripPlanAdapter.TripViewHolder>() {

    private var items: List<TripPlan> = emptyList()

    fun submitList(newItems: List<TripPlan>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_plan, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.tripItemTitle)
        private val dates = itemView.findViewById<TextView>(R.id.tripItemDates)
        private val notes = itemView.findViewById<TextView>(R.id.tripItemNotes)
        private val count = itemView.findViewById<TextView>(R.id.tripItemCount)

        fun bind(trip: TripPlan) {
            val context = itemView.context
            title.text = trip.title
            dates.text = TripDateFormatter.range(trip.startDate, trip.endDate)
            notes.text = trip.notes
            notes.visibility = if (trip.notes.isBlank()) View.GONE else View.VISIBLE
            count.text = context.getString(R.string.trip_items_count, trip.items.size)
            itemView.contentDescription = trip.title
            itemView.setOnClickListener { onOpen(trip) }
        }
    }
}
