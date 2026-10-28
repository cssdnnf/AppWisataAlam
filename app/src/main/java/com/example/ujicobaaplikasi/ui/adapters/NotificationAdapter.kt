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
import com.example.ujicobaaplikasi.data.AppNotification
import com.google.android.material.card.MaterialCardView

data class NotificationRow(
    val notification: AppNotification,
    val unread: Boolean,
)

class NotificationAdapter(
    private val onOpen: (AppNotification) -> Unit,
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private var rows: List<NotificationRow> = emptyList()

    fun submitList(newRows: List<NotificationRow>) {
        rows = newRows
        notifyDataSetChanged()
    }

    fun markAllRead() {
        rows = rows.map { it.copy(unread = false) }
        notifyDataSetChanged()
    }

    fun hasUnread(): Boolean = rows.any { it.unread }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(rows[position])
    }

    override fun getItemCount(): Int = rows.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView as MaterialCardView
        private val iconContainer = itemView.findViewById<View>(R.id.notifIconContainer)
        private val icon = itemView.findViewById<ImageView>(R.id.notifIcon)
        private val title = itemView.findViewById<TextView>(R.id.notifTitle)
        private val message = itemView.findViewById<TextView>(R.id.notifMessage)
        private val time = itemView.findViewById<TextView>(R.id.notifTime)
        private val dot = itemView.findViewById<View>(R.id.notifDot)

        fun bind(row: NotificationRow) {
            val context = itemView.context
            val notification = row.notification
            val accent = ContextCompat.getColor(context, notification.tintRes)
            val container = ContextCompat.getColor(context, notification.containerColorRes)
            card.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (row.unread) R.color.hijau_lembut_ringan else R.color.permukaan,
                ),
            )
            iconContainer.backgroundTintList = ColorStateList.valueOf(container)
            icon.setImageResource(notification.iconRes)
            icon.imageTintList = ColorStateList.valueOf(accent)
            title.text = notification.title
            message.text = notification.message
            time.text = notification.time
            dot.visibility = if (row.unread) View.VISIBLE else View.GONE
            card.contentDescription = notification.title
            card.setOnClickListener { onOpen(notification) }
        }
    }
}
