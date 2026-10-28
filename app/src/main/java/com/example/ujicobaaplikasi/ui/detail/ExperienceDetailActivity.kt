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
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.Experience
import com.example.ujicobaaplikasi.data.ExperienceType
import com.example.ujicobaaplikasi.data.ImageTools
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.navigation.BottomNavView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ExperienceDetailActivity : BaseActivity() {

    private lateinit var experience: Experience
    private lateinit var saveButton: MaterialButton
    private lateinit var favoriteButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_detail)

        val type = runCatching {
            ExperienceType.valueOf(intent.getStringExtra(EXTRA_TYPE).orEmpty())
        }.getOrNull()
        val selected = type?.let {
            TravelRepository.experienceById(it, intent.getStringExtra(EXTRA_ID))
        }
        if (selected == null) {
            AppNotice.deferError(this, R.string.experience_not_found)
            finish()
            return
        }
        experience = selected

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
            ImageTools.bind(
                this,
                experience.imageRes,
                experience.imageBytes,
                "exp-detail-${experience.type.name}-${experience.id}",
            )
            contentDescription = experience.title
        }
        findViewById<TextView>(R.id.detailCategory).text = experience.category.ifBlank {
            getString(experience.type.labelRes)
        }
        findViewById<TextView>(R.id.detailTitle).text = experience.title
        findViewById<TextView>(R.id.detailLocation).text =
            experience.location.ifBlank { experience.subtitle }
        findViewById<TextView>(R.id.detailRating).text =
            if (experience.rating > 0) experience.rating.toString() else "—"
        findViewById<TextView>(R.id.detailReviews).text =
            getString(R.string.format_reviews, experience.reviewCount)
        findViewById<TextView>(R.id.detailPrice).text =
            experience.price.ifBlank { experience.badge.ifBlank { "—" } }
        findViewById<TextView>(R.id.detailDuration).text =
            experience.duration.ifBlank { "—" }
        findViewById<TextView>(R.id.detailHours).text =
            experience.openHours.ifBlank { "—" }
        findViewById<TextView>(R.id.detailDescription).text = experience.description
        findViewById<TextView>(R.id.detailBestTime).text =
            experience.bestTime.ifBlank { experience.badge.ifBlank { "—" } }
        findViewById<TextView>(R.id.detailMapAddress).text =
            experience.mapQuery.ifBlank { experience.location }

        val priceLabel = when (experience.type) {
            ExperienceType.LODGING -> R.string.experience_fact_price_night
            ExperienceType.ACTIVITY -> R.string.experience_fact_price
            ExperienceType.EVENT -> R.string.experience_fact_ticket
        }
        val aboutTitle = when (experience.type) {
            ExperienceType.LODGING -> R.string.experience_about_lodging
            ExperienceType.ACTIVITY -> R.string.experience_about_activity
            ExperienceType.EVENT -> R.string.experience_about_event
        }
        findViewById<TextView>(R.id.detailFactPriceLabel).setText(priceLabel)
        findViewById<TextView>(R.id.detailFactDurationLabel)
            .setText(R.string.experience_fact_duration)
        findViewById<TextView>(R.id.detailFactHoursLabel)
            .setText(R.string.experience_fact_schedule)
        findViewById<TextView>(R.id.detailAboutTitle).setText(aboutTitle)
        if (experience.type == ExperienceType.EVENT) {
            findViewById<TextView>(R.id.detailBestTimeLabel).setText(R.string.experience_period)
        }

        addChips(findViewById(R.id.highlightChips), experience.highlights)
        addChips(findViewById(R.id.facilityChips), experience.facilities)
    }

    private fun bindActions() {
        saveButton = findViewById(R.id.tombolSimpanDestinasi)
        favoriteButton = findViewById(R.id.tombolFavoritDetail)

        findViewById<android.view.View>(R.id.tombolKembaliDetail)
            .setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.tombolBagikanDetail)
            .setOnClickListener { share() }
        findViewById<android.view.View>(R.id.tombolBukaMaps)
            .setOnClickListener { openMap() }

        saveButton.setText(R.string.experience_save)
        saveButton.setOnClickListener { toggleSaved(showNotice = true) }
        favoriteButton.setOnClickListener { toggleSaved(showNotice = true) }
        updateSavedState()
    }

    private fun toggleSaved(showNotice: Boolean) {
        val type = experience.type.savedType
        AppData.toggleSaved(this, type, experience.id) { result ->
            result.fold(
                onSuccess = { nowSaved ->
                    updateSavedState()
                    if (showNotice) {
                        if (nowSaved) {
                            AppNotice.success(this, R.string.favorite_added)
                        } else {
                            AppNotice.info(this, R.string.favorite_removed)
                        }
                    }
                },
                onFailure = { AppNotice.error(this, R.string.error_network) },
            )
        }
    }

    private fun updateSavedState() {
        if (!::experience.isInitialized) return
        val saved = AppData.isSaved(experience.type.savedType, experience.id)
        saveButton.setText(if (saved) R.string.experience_saved else R.string.experience_save)
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
                getString(R.string.detail_share_text, experience.title),
            )
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
    }

    private fun openMap() {
        val query = experience.mapQuery.ifBlank { "${experience.title}, Lombok" }
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
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
        private const val EXTRA_TYPE = "extra_experience_type"
        private const val EXTRA_ID = "extra_experience_id"
        private const val EXTRA_NAV_DESTINATION = "extra_nav_destination"

        fun open(
            context: Context,
            type: ExperienceType,
            id: String,
            navDestination: BottomNavDestination = BottomNavDestination.HOME,
        ) {
            context.startActivity(
                Intent(context, ExperienceDetailActivity::class.java)
                    .putExtra(EXTRA_TYPE, type.name)
                    .putExtra(EXTRA_ID, id)
                    .putExtra(EXTRA_NAV_DESTINATION, navDestination.name),
            )
        }
    }
}
