package com.example.ujicobaaplikasi.ui.detail

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.AppNotification
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.adapters.NotificationAdapter
import com.example.ujicobaaplikasi.ui.adapters.NotificationRow
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.navigation.BottomNavView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NotificationsActivity : BaseActivity() {

    private lateinit var adapter: NotificationAdapter
    private lateinit var markAllButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<View>(R.id.notifHeader).applyStatusBarInset()
        findViewById<View>(R.id.tombolKembaliNotif).setOnClickListener { finish() }

        val nav = findViewById<BottomNavView>(R.id.notifBottomNav)
        val selected = runCatching {
            BottomNavDestination.valueOf(
                intent.getStringExtra(EXTRA_NAV_DESTINATION)
                    ?: BottomNavDestination.PROFILE.name,
            )
        }.getOrDefault(BottomNavDestination.PROFILE)
        nav.selectedDestination = selected
        nav.setOnDestinationSelectedListener { destination ->
            MainActivity.open(this, destination)
            finish()
        }

        adapter = NotificationAdapter { notification -> showNotification(notification) }
        val recycler = findViewById<RecyclerView>(R.id.notifRecycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.itemAnimator = null
        recycler.adapter = adapter

        markAllButton = findViewById(R.id.tombolTandaiDibaca)
        renderNotifications()

        markAllButton.setOnClickListener {
            markAllButton.isEnabled = false
            AppData.markAllNotificationsRead(this) { result ->
                result.fold(
                    onSuccess = {
                        adapter.markAllRead()
                        updateMarkAllButton(false)
                        AppNotice.success(this, R.string.notifications_marked)
                    },
                    onFailure = {
                        markAllButton.isEnabled = true
                        AppNotice.error(this, R.string.error_network)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        renderNotifications()
    }

    private fun renderNotifications() {
        val unread = AppData.unreadNotificationIds()
        adapter.submitList(
            TravelRepository.notifications.map { NotificationRow(it, it.id in unread) },
        )
        updateMarkAllButton(unread.isNotEmpty())
    }

    private fun updateMarkAllButton(enabled: Boolean) {
        markAllButton.isEnabled = enabled
        markAllButton.alpha = if (enabled) 1f else 0.45f
    }

    private fun showNotification(notification: AppNotification) {
        MaterialAlertDialogBuilder(this)
            .setTitle(notification.title)
            .setMessage(notification.message)
            .setPositiveButton(R.string.action_close, null)
            .show()
    }

    companion object {
        const val EXTRA_NAV_DESTINATION = "extra_nav_destination"
    }
}
