package com.example.ujicobaaplikasi.ui.detail

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.Destination
import com.example.ujicobaaplikasi.data.Experience
import com.example.ujicobaaplikasi.data.MenuEntry
import com.example.ujicobaaplikasi.data.SavedType
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.adapters.CulinaryAdapter
import com.example.ujicobaaplikasi.ui.adapters.DestinationAdapter
import com.example.ujicobaaplikasi.ui.adapters.ExperienceAdapter
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.navigation.BottomNavView
import com.example.ujicobaaplikasi.ui.trip.TripPlansActivity
import com.google.android.material.card.MaterialCardView

class MenuDetailActivity : BaseActivity() {

    private var destinationAdapter: DestinationAdapter? = null
    private var culinaryAdapter: CulinaryAdapter? = null
    private var experienceAdapter: ExperienceAdapter? = null
    private var experienceSavedType: SavedType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_detail)

        val menu = TravelRepository.menuById(intent.getStringExtra(EXTRA_MENU_ID))
        if (menu == null) {
            finish()
            return
        }
        if (menu.id == "trip") {
            TripPlansActivity.open(this)
            finish()
            return
        }

        findViewById<View>(R.id.menuDetailHeader).applyStatusBarInset()
        findViewById<View>(R.id.tombolKembaliMenu).setOnClickListener { finish() }

        bindBanner(menu)
        bindList(menu)

        val nav = findViewById<BottomNavView>(R.id.menuDetailBottomNav)
        nav.selectedDestination = BottomNavDestination.HOME
        nav.setOnDestinationSelectedListener { target ->
            MainActivity.open(this, target)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        destinationAdapter?.updateFavorites(AppData.favoriteIds())
        culinaryAdapter?.updateFavorites(AppData.savedIds(SavedType.CULINARY))
        experienceSavedType?.let { type ->
            experienceAdapter?.updateFavorites(AppData.savedIds(type))
        }
    }

    private fun bindBanner(menu: MenuEntry) {
        findViewById<TextView>(R.id.menuDetailTitle).text = getString(menu.titleRes)
        findViewById<TextView>(R.id.menuBannerSubtitle).text = getString(menu.pageSubtitleRes)

        val accent = ContextCompat.getColor(this, menu.iconTintRes)
        val container = ContextCompat.getColor(this, menu.containerColorRes)
        val banner = findViewById<MaterialCardView>(R.id.menuBanner)
        banner.setCardBackgroundColor(container)

        findViewById<ImageView>(R.id.menuBannerIcon).apply {
            setImageResource(menu.iconRes)
            imageTintList = ColorStateList.valueOf(accent)
        }
        findViewById<TextView>(R.id.menuBannerCount).apply {
            text = countLabel(menu)
            setTextColor(accent)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.tombolSimpanRencana)
            .visibility = View.GONE
    }

    private fun bindList(menu: MenuEntry) {
        val recycler = findViewById<RecyclerView>(R.id.menuDetailRecycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.itemAnimator = null

        when (menu.id) {
            "destination" -> {
                val adapter = DestinationAdapter(
                    onOpen = { destination ->
                        DestinationDetailActivity.open(this, destination.id)
                    },
                    onFavoriteToggle = ::toggleFavorite,
                )
                destinationAdapter = adapter
                recycler.adapter = adapter
                adapter.submitList(
                    TravelRepository.destinations,
                    AppData.favoriteIds(),
                )
            }

            "culinary" -> {
                val adapter = CulinaryAdapter(
                    onOpen = { culinary -> CulinaryDetailActivity.open(this, culinary.id) },
                    onFavoriteToggle = ::toggleCulinaryFavorite,
                )
                culinaryAdapter = adapter
                recycler.adapter = adapter
                adapter.submitList(
                    TravelRepository.culinary,
                    AppData.savedIds(SavedType.CULINARY),
                )
            }

            else -> {
                val type = when (menu.id) {
                    "stay" -> SavedType.LODGING
                    "activity" -> SavedType.ACTIVITY
                    "event" -> SavedType.EVENT
                    else -> null
                } ?: return
                val adapter = ExperienceAdapter(
                    onOpen = { experience ->
                        ExperienceDetailActivity.open(
                            this,
                            experience.type,
                            experience.id,
                            BottomNavDestination.HOME,
                        )
                    },
                    onFavoriteToggle = { experience -> toggleExperienceFavorite(experience) },
                )
                experienceAdapter = adapter
                experienceSavedType = type
                recycler.adapter = adapter
                adapter.submitList(
                    TravelRepository.experiencesForMenu(menu.id),
                    AppData.savedIds(type),
                )
            }
        }
    }

    private fun countLabel(menu: MenuEntry): String {
        return when (menu.id) {
            "destination" -> getString(
                R.string.menu_count_destination,
                TravelRepository.destinations.size,
            )
            "culinary" -> getString(
                R.string.menu_count_culinary,
                TravelRepository.culinary.size,
            )
            "stay" -> getString(
                R.string.menu_count_stay,
                TravelRepository.experiencesForMenu("stay").size,
            )
            "activity" -> getString(
                R.string.menu_count_activity,
                TravelRepository.experiencesForMenu("activity").size,
            )
            "event" -> getString(
                R.string.menu_count_event,
                TravelRepository.experiencesForMenu("event").size,
            )
            else -> ""
        }
    }

    private fun toggleFavorite(destination: Destination) {
        AppData.toggleFavorite(this, destination.id) { result ->
            result.fold(
                onSuccess = { nowFavorite ->
                    destinationAdapter?.updateFavorites(AppData.favoriteIds())
                    if (nowFavorite) {
                        AppNotice.success(this, R.string.favorite_added)
                    } else {
                        AppNotice.info(this, R.string.favorite_removed)
                    }
                },
                onFailure = {
                    AppNotice.error(this, R.string.error_network)
                },
            )
        }
    }

    private fun toggleCulinaryFavorite(culinary: com.example.ujicobaaplikasi.data.Culinary) {
        AppData.toggleSaved(this, SavedType.CULINARY, culinary.id) { result ->
            result.fold(
                onSuccess = { nowSaved ->
                    culinaryAdapter?.updateFavorites(AppData.savedIds(SavedType.CULINARY))
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

    private fun toggleExperienceFavorite(experience: Experience) {
        val type = experience.type.savedType
        AppData.toggleSaved(this, type, experience.id) { result ->
            result.fold(
                onSuccess = { nowSaved ->
                    experienceAdapter?.updateFavorites(AppData.savedIds(type))
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

    companion object {
        private const val EXTRA_MENU_ID = "extra_menu_id"

        fun open(context: Context, menuId: String) {
            context.startActivity(
                Intent(context, MenuDetailActivity::class.java)
                    .putExtra(EXTRA_MENU_ID, menuId),
            )
        }
    }
}
