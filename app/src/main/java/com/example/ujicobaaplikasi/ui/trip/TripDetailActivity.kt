package com.example.ujicobaaplikasi.ui.trip

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.data.TripItem
import com.example.ujicobaaplikasi.data.TripPlan
import com.example.ujicobaaplikasi.ui.adapters.TripItemAdapter
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TripDetailActivity : BaseActivity() {

    private var tripId: Long = 0
    private lateinit var adapter: TripItemAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: View
    private lateinit var titleView: TextView
    private lateinit var subtitleView: TextView
    private lateinit var notesView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)

        tripId = intent.getLongExtra(EXTRA_TRIP_ID, 0)
        if (tripId == 0L) {
            finish()
            return
        }

        findViewById<View>(R.id.tripDetailHeader).applyStatusBarInset()
        titleView = findViewById(R.id.tripDetailTitle)
        subtitleView = findViewById(R.id.tripDetailSub)
        notesView = findViewById(R.id.tripDetailNotes)
        recycler = findViewById(R.id.tripDetailRecycler)
        emptyView = findViewById(R.id.tripDetailEmpty)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.itemAnimator = null
        adapter = TripItemAdapter(
            onOpen = { item -> editItem(item) },
            onDelete = { item -> confirmDeleteItem(item) },
        )
        recycler.adapter = adapter

        findViewById<View>(R.id.tombolKembaliTripDetail).setOnClickListener { finish() }
        findViewById<View>(R.id.tombolEditTrip).setOnClickListener { editPlan() }
        findViewById<View>(R.id.tombolHapusTrip).setOnClickListener { confirmDeletePlan() }
        findViewById<View>(R.id.tombolTambahItem).setOnClickListener { addItem() }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun currentTrip(): TripPlan? =
        TravelRepository.trips.firstOrNull { it.id == tripId }

    private fun refresh() {
        val trip = currentTrip()
        if (trip == null) {
            finish()
            return
        }
        titleView.text = trip.title
        val dates = TripDateFormatter.range(trip.startDate, trip.endDate)
        subtitleView.text = dates.ifBlank { getString(R.string.trip_no_date) }
        notesView.text = trip.notes
        notesView.visibility = if (trip.notes.isBlank()) View.GONE else View.VISIBLE
        adapter.submitList(trip.items)
        emptyView.visibility = if (trip.items.isEmpty()) View.VISIBLE else View.GONE
        recycler.visibility = if (trip.items.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun editPlan() {
        val trip = currentTrip() ?: return
        TripDialogs.showPlanDialog(this, trip) { refresh() }
    }

    private fun addItem() {
        val trip = currentTrip() ?: return
        val nextDay = (trip.items.maxOfOrNull { it.dayNumber } ?: 0) + 1
        TripDialogs.showItemDialog(this, tripId, null, nextDay) { refresh() }
    }

    private fun editItem(item: TripItem) {
        TripDialogs.showItemDialog(this, tripId, item, item.dayNumber) { refresh() }
    }

    private fun confirmDeletePlan() {
        val trip = currentTrip() ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.trip_delete_title)
            .setMessage(R.string.trip_delete_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                AppData.deleteTrip(this, trip.id) { result ->
                    result.fold(
                        onSuccess = {
                            AppNotice.deferSuccess(this, R.string.trip_deleted)
                            finish()
                        },
                        onFailure = {
                            AppNotice.error(
                                this,
                                it.message ?: getString(R.string.error_network),
                            )
                        },
                    )
                }
            }
            .show()
    }

    private fun confirmDeleteItem(item: TripItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.trip_item_delete_title)
            .setMessage(item.title)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                AppData.deleteTripItem(this, tripId, item.id) { result ->
                    result.fold(
                        onSuccess = {
                            AppNotice.info(this, R.string.trip_item_deleted)
                            refresh()
                        },
                        onFailure = {
                            AppNotice.error(
                                this,
                                it.message ?: getString(R.string.error_network),
                            )
                        },
                    )
                }
            }
            .show()
    }

    companion object {
        private const val EXTRA_TRIP_ID = "extra_trip_id"

        fun open(context: Context, tripId: Long) {
            context.startActivity(
                Intent(context, TripDetailActivity::class.java)
                    .putExtra(EXTRA_TRIP_ID, tripId),
            )
        }
    }
}
