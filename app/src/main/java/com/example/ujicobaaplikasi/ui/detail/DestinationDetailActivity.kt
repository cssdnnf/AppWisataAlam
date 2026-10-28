package com.example.ujicobaaplikasi.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.ImageTools
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.Destination
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.navigation.BottomNavView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class DestinationDetailActivity : BaseActivity() {

    private lateinit var destination: Destination
    private lateinit var saveButton: MaterialButton
    private lateinit var favoriteButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_detail)

        val selected = TravelRepository.destinationById(
            intent.getStringExtra(EXTRA_DESTINATION_ID),
        )
        if (selected == null) {
            AppNotice.deferError(this, R.string.detail_not_found)
            finish()
            return
        }
        destination = selected

        findViewById<android.view.View>(R.id.destinationRoot).applyStatusBarInset()

        bindContent()
        bindActions()

        val nav = findViewById<BottomNavView>(R.id.detailBottomNav)
        nav.selectedDestination = sourceDestination()
        nav.setOnDestinationSelectedListener { target ->
            MainActivity.open(this, target)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateSavedState()
    }

    private fun bindContent() {
        findViewById<ImageView>(R.id.detailHeroImage).apply {
            ImageTools.bind(this, destination.imageRes, destination.imageBytes, "dest-detail-${destination.id}")
            contentDescription = destination.name
        }
        findViewById<TextView>(R.id.detailCategory).text = destination.category
        findViewById<TextView>(R.id.detailTitle).text = destination.name
        findViewById<TextView>(R.id.detailLocation).text = destination.location
        findViewById<TextView>(R.id.detailRating).text = destination.rating.toString()
        findViewById<TextView>(R.id.detailReviews).text =
            getString(R.string.format_reviews, destination.reviewCount)
        findViewById<TextView>(R.id.detailPrice).text = destination.price
        findViewById<TextView>(R.id.detailDuration).text = destination.duration
        findViewById<TextView>(R.id.detailHours).text = destination.openHours
        findViewById<TextView>(R.id.detailDescription).text = destination.description
        findViewById<TextView>(R.id.detailBestTime).text = destination.bestTime
        findViewById<TextView>(R.id.detailMapAddress).text = destination.mapQuery

        addChips(findViewById(R.id.highlightChips), destination.highlights)
        addChips(findViewById(R.id.facilityChips), destination.facilities)
    }

    private fun bindActions() {
        saveButton = findViewById(R.id.tombolSimpanDestinasi)
        favoriteButton = findViewById(R.id.tombolFavoritDetail)

        findViewById<android.view.View>(R.id.tombolKembaliDetail).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.tombolBagikanDetail).setOnClickListener { share() }
        findViewById<android.view.View>(R.id.tombolBukaMaps).setOnClickListener { openMap() }

        saveButton.setOnClickListener { toggleSaved(showToast = true) }
        favoriteButton.setOnClickListener { toggleSaved(showToast = true) }
        updateSavedState()
    }

    private fun toggleSaved(showToast: Boolean) {
        AppData.toggleFavorite(this, destination.id) { result ->
            result.fold(
                onSuccess = { nowSaved ->
                    updateSavedState()
                    if (showToast) {
                        if (nowSaved) {
                            AppNotice.success(this, R.string.favorite_added)
                        } else {
                            AppNotice.info(this, R.string.favorite_removed)
                        }
                    }
                },
                onFailure = {
                    AppNotice.error(this, R.string.error_network)
                },
            )
        }
    }

    private fun updateSavedState() {
        if (!::destination.isInitialized) return
        val saved = AppData.isFavorite(destination.id)
        saveButton.setText(if (saved) R.string.detail_saved else R.string.detail_save)
        saveButton.setIconResource(
            if (saved) R.drawable.ic_heart_filled else R.drawable.ic_heart,
        )
        favoriteButton.setIconResource(
            if (saved) R.drawable.ic_heart_filled else R.drawable.ic_heart,
        )
        favoriteButton.setIconTintResource(
            if (saved) R.color.koral else R.color.white,
        )
        favoriteButton.contentDescription = getString(
            if (saved) R.string.cd_unfavorite else R.string.cd_favorite,
        )
    }

    private fun addChips(group: ChipGroup, labels: List<String>) {
        group.removeAllViews()
        labels.forEach { label ->
            val chip = Chip(this)
            chip.text = label
            chip.isClickable = false
            chip.isCheckable = false
            chip.isFocusable = false
            chip.isCheckedIconVisible = false
            chip.chipMinHeight = 30 * resources.displayMetrics.density
            chip.setEnsureMinTouchTargetSize(false)
            chip.chipStrokeWidth = resources.displayMetrics.density
            chip.chipStrokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.hijau_lombok_muda),
            )
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.hijau_lembut),
            )
            chip.setTextColor(ContextCompat.getColor(this, R.color.teks_utama))
            chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
            group.addView(chip)
        }
    }

    private fun share() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.detail_share_text, destination.name),
            )
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
    }

    private fun openMap() {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(destination.mapQuery)}")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            AppNotice.error(this, R.string.detail_maps_error)
        }
    }

    private fun sourceDestination(): BottomNavDestination {
        return runCatching {
            BottomNavDestination.valueOf(
                intent.getStringExtra(EXTRA_NAV_DESTINATION) ?: BottomNavDestination.HOME.name,
            )
        }.getOrDefault(BottomNavDestination.HOME)
    }

    companion object {
        private const val EXTRA_DESTINATION_ID = "extra_destination_id"
        private const val EXTRA_NAV_DESTINATION = "extra_nav_destination"

        fun open(
            context: Context,
            destinationId: String,
            navDestination: BottomNavDestination = BottomNavDestination.HOME,
        ) {
            context.startActivity(
                Intent(context, DestinationDetailActivity::class.java)
                    .putExtra(EXTRA_DESTINATION_ID, destinationId)
                    .putExtra(EXTRA_NAV_DESTINATION, navDestination.name),
            )
        }
    }
}
