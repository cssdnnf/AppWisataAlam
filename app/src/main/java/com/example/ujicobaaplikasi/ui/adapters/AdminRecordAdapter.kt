package com.example.ujicobaaplikasi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.AdminRecord

class AdminRecordAdapter(
    private val onOpen: (AdminRecord) -> Unit,
    private val onDelete: (AdminRecord) -> Unit,
) : RecyclerView.Adapter<AdminRecordAdapter.RecordViewHolder>() {

    private var items: List<AdminRecord> = emptyList()

    fun submitList(newItems: List<AdminRecord>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.adminRecordTitle)
        private val subtitle = itemView.findViewById<TextView>(R.id.adminRecordSubtitle)
        private val delete = itemView.findViewById<View>(R.id.adminRecordDelete)

        fun bind(record: AdminRecord) {
            title.text = record.title
            subtitle.text = record.subtitle
            itemView.contentDescription = record.title
            itemView.setOnClickListener { onOpen(record) }
            delete.setOnClickListener { onDelete(record) }
        }
    }
}
