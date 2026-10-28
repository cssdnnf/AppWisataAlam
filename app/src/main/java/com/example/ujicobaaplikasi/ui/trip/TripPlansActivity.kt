package com.example.ujicobaaplikasi.ui.trip

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.adapters.TripPlanAdapter
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset

class TripPlansActivity : BaseActivity() {

    private lateinit var adapter: TripPlanAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_plans)

        findViewById<View>(R.id.tripPlansHeader).applyStatusBarInset()
        findViewById<View>(R.id.tombolKembaliTripPlans).setOnClickListener { finish() }

        recycler = findViewById(R.id.tripPlansRecycler)
        emptyView = findViewById(R.id.tripPlansEmpty)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.itemAnimator = null
        adapter = TripPlanAdapter { trip ->
            TripDetailActivity.open(this, trip.id)
        }
        recycler.adapter = adapter

        findViewById<View>(R.id.tombolTambahTrip).setOnClickListener {
            TripDialogs.showPlanDialog(this, null) { refresh() }
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val trips = TravelRepository.trips
        adapter.submitList(trips)
        emptyView.visibility = if (trips.isEmpty()) View.VISIBLE else View.GONE
        recycler.visibility = if (trips.isEmpty()) View.GONE else View.VISIBLE
    }

    companion object {
        fun open(context: Context) {
            context.startActivity(Intent(context, TripPlansActivity::class.java))
        }
    }
}
