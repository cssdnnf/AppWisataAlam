package com.example.ujicobaaplikasi.ui.detail

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.ImageTools
import com.example.ujicobaaplikasi.data.Culinary
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.Destination
import com.example.ujicobaaplikasi.data.SavedType
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.navigation.BottomNavView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class CulinaryDetailActivity : BaseActivity() {

    private lateinit var culinary: Culinary
    private lateinit var favoriteButton: MaterialButton
    private var relatedDestination: Destination? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_culinary_detail)

        val selected = TravelRepository.culinaryById(intent.getStringExtra(EXTRA_CULINARY_ID))
        if (selected == null) {
            AppNotice.deferError(this, R.string.culinary_not_found)
            finish()
            return
        }
        culinary = selected

        findViewById<android.view.View>(R.id.culinaryRoot).applyStatusBarInset()

        bindContent()
        bindActions()

        val nav = findViewById<BottomNavView>(R.id.culinaryBottomNav)
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
        findViewById<ImageView>(R.id.culinaryHeroImage).apply {
            ImageTools.bind(this, culinary.imageRes, culinary.imageBytes, "cul-detail-${culinary.id}")
            contentDescription = culinary.name
        }
        findViewById<TextView>(R.id.culinaryTitle).text = culinary.name
        findViewById<TextView>(R.id.culinaryOrigin).text = culinary.origin
        findViewById<TextView>(R.id.culinaryRating).text = culinary.rating.toString()
        findViewById<TextView>(R.id.culinaryPrice).text = culinary.price
        findViewById<TextView>(R.id.culinaryReviews).text = culinary.reviewCount
        findViewById<TextView>(R.id.culinaryDescription).text = culinary.description
        findViewById<TextView>(R.id.culinaryBestAt).text = culinary.bestAt
        findViewById<TextView>(R.id.culinaryTips).text = culinary.tips

        addChips(findViewById(R.id.flavorChips), culinary.flavorTags)

        relatedDestination = TravelRepository.destinationById(culinary.relatedDestinationId)
        relatedDestination?.let { destination ->
            findViewById<ImageView>(R.id.culinaryRelatedImage).apply {
                ImageTools.bind(this, destination.imageRes, destination.imageBytes, "cul-related-${destination.id}")
                contentDescription = destination.name
            }
            findViewById<TextView>(R.id.culinaryRelatedName).text = destination.name
            findViewById<TextView>(R.id.culinaryRelatedLocation).text = destination.location
            findViewById<android.view.View>(R.id.culinaryRelatedCard).setOnClickListener {
                DestinationDetailActivity.open(this, destination.id)
            }
        } ?: run {
            findViewById<android.view.View>(R.id.culinaryRelatedCard).visibility = android.view.View.GONE
        }
    }

    private fun bindActions() {
        favoriteButton = findViewById(R.id.tombolFavoritCulinary)
        findViewById<android.view.View>(R.id.tombolKembaliCulinary).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.tombolBagikanCulinary).setOnClickListener { share() }
        favoriteButton.setOnClickListener { toggleSaved() }
        updateSavedState()
    }

    private fun toggleSaved() {
        AppData.toggleSaved(this, SavedType.CULINARY, culinary.id) { result ->
            result.fold(
                onSuccess = { nowSaved ->
                    updateSavedState()
                    if (nowSaved) {
                        AppNotice.success(this, R.string.favorite_added)
                    } else {
                        AppNotice.info(this, R.string.favorite_removed)
                    }
                },
                onFailure = { AppNotice.error(this, R.string.error_network) },
            )
        }
    }

    private fun updateSavedState() {
        if (!::culinary.isInitialized || !::favoriteButton.isInitialized) return
        val saved = AppData.isSaved(SavedType.CULINARY, culinary.id)
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
            chip.chipStrokeWidth = 0f
            chip.chipMinHeight = 30 * resources.displayMetrics.density
            chip.setEnsureMinTouchTargetSize(false)
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.koral_lembut),
            )
            chip.setTextColor(ContextCompat.getColor(this, R.color.koral_gelap))
            chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
            group.addView(chip)
        }
    }

    private fun share() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.detail_share_text, culinary.name),
            )
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
    }

    private fun sourceDestination(): BottomNavDestination {
        return runCatching {
            BottomNavDestination.valueOf(
                intent.getStringExtra(EXTRA_NAV_DESTINATION) ?: BottomNavDestination.HOME.name,
            )
        }.getOrDefault(BottomNavDestination.HOME)
    }

    companion object {
        private const val EXTRA_CULINARY_ID = "extra_culinary_id"
        private const val EXTRA_NAV_DESTINATION = "extra_nav_destination"

        fun open(
            context: Context,
            culinaryId: String,
            navDestination: BottomNavDestination = BottomNavDestination.HOME,
        ) {
            context.startActivity(
                Intent(context, CulinaryDetailActivity::class.java)
                    .putExtra(EXTRA_CULINARY_ID, culinaryId)
                    .putExtra(EXTRA_NAV_DESTINATION, navDestination.name),
            )
        }
    }
}
