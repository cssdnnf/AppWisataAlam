package com.example.ujicobaaplikasi.ui.main

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.SavedType
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.adapters.SavedEntry
import com.example.ujicobaaplikasi.ui.adapters.SavedEntryAdapter
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.ui.detail.CulinaryDetailActivity
import com.example.ujicobaaplikasi.ui.detail.DestinationDetailActivity
import com.example.ujicobaaplikasi.ui.detail.ExperienceDetailActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private var adapter: SavedEntryAdapter? = null
    private var countView: TextView? = null
    private var emptyView: View? = null
    private var recycler: RecyclerView? = null
    private var selectedType: SavedType? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countView = view.findViewById(R.id.savedCount)
        emptyView = view.findViewById(R.id.emptySaved)
        recycler = view.findViewById(R.id.savedRecycler)
        recycler?.layoutManager = LinearLayoutManager(requireContext())
        recycler?.itemAnimator = null

        adapter = SavedEntryAdapter(
            onOpen = ::openEntry,
            onRemove = ::removeEntry,
        )
        recycler?.adapter = adapter

        view.findViewById<View>(R.id.tombolMulaiJelajah).setOnClickListener {
            (activity as? MainActivity)?.showTab(BottomNavDestination.EXPLORE)
        }

        val chips = view.findViewById<ChipGroup>(R.id.savedFilterChips)
        chips.setOnCheckedStateChangeListener { group, checkedIds ->
            val checked = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            selectedType = when (checked?.id) {
                R.id.chipSavedDestinasi -> SavedType.DESTINATION
                R.id.chipSavedKuliner -> SavedType.CULINARY
                R.id.chipSavedPenginapan -> SavedType.LODGING
                R.id.chipSavedAktivitas -> SavedType.ACTIVITY
                R.id.chipSavedEvent -> SavedType.EVENT
                else -> null
            }
            refresh()
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) refresh()
    }

    private fun refresh() {
        val all = buildEntries()
        val type = selectedType
        val filtered = if (type == null) all else all.filter { it.type == type }
        adapter?.submitList(filtered)
        countView?.text = getString(R.string.format_saved_count, filtered.size)
        emptyView?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recycler?.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun buildEntries(): List<SavedEntry> {
        val entries = mutableListOf<SavedEntry>()

        val destinationIds = AppData.savedIds(SavedType.DESTINATION)
        TravelRepository.destinations
            .filter { it.id in destinationIds }
            .forEach { item ->
                entries += SavedEntry(
                    type = SavedType.DESTINATION,
                    id = item.id,
                    title = item.name,
                    subtitle = item.location,
                    category = item.category,
                    imageRes = item.imageRes,
                    imageBytes = item.imageBytes,
                    rating = item.rating.toString(),
                    reviewCount = item.reviewCount,
                    price = item.price,
                )
            }

        val culinaryIds = AppData.savedIds(SavedType.CULINARY)
        TravelRepository.culinary
            .filter { it.id in culinaryIds }
            .forEach { item ->
                entries += SavedEntry(
                    type = SavedType.CULINARY,
                    id = item.id,
                    title = item.name,
                    subtitle = item.origin,
                    category = item.category,
                    imageRes = item.imageRes,
                    imageBytes = item.imageBytes,
                    rating = item.rating.toString(),
                    reviewCount = item.reviewCount,
                    price = item.price,
                )
            }

        addExperienceEntries(entries, TravelRepository.lodging, SavedType.LODGING)
        addExperienceEntries(entries, TravelRepository.activities, SavedType.ACTIVITY)
        addExperienceEntries(entries, TravelRepository.events, SavedType.EVENT)
        return entries
    }

    private fun addExperienceEntries(
        target: MutableList<SavedEntry>,
        items: List<com.example.ujicobaaplikasi.data.Experience>,
        type: SavedType,
    ) {
        val ids = AppData.savedIds(type)
        items.filter { it.id in ids }.forEach { item ->
            target += SavedEntry(
                type = type,
                id = item.id,
                title = item.title,
                subtitle = item.location.ifBlank { item.subtitle },
                category = item.category.ifBlank { getString(item.type.labelRes) },
                imageRes = item.imageRes,
                imageBytes = item.imageBytes,
                rating = if (item.rating > 0) item.rating.toString() else "",
                reviewCount = item.reviewCount,
                price = item.price.ifBlank { item.badge },
            )
        }
    }

    private fun openEntry(entry: SavedEntry) {
        when (entry.type) {
            SavedType.DESTINATION -> DestinationDetailActivity.open(
                requireContext(),
                entry.id,
                BottomNavDestination.SAVED,
            )
            SavedType.CULINARY -> CulinaryDetailActivity.open(
                requireContext(),
                entry.id,
                BottomNavDestination.SAVED,
            )
            SavedType.LODGING,
            SavedType.ACTIVITY,
            SavedType.EVENT,
            -> {
                val experience = when (entry.type) {
                    SavedType.LODGING -> TravelRepository.lodging
                    SavedType.ACTIVITY -> TravelRepository.activities
                    else -> TravelRepository.events
                }.firstOrNull { it.id == entry.id } ?: return
                ExperienceDetailActivity.open(
                    requireContext(),
                    experience.type,
                    experience.id,
                    BottomNavDestination.SAVED,
                )
            }
        }
    }

    private fun removeEntry(entry: SavedEntry) {
        AppData.toggleSaved(requireContext(), entry.type, entry.id) { result ->
            result.fold(
                onSuccess = {
                    AppNotice.info(requireActivity(), R.string.favorite_removed)
                    refresh()
                },
                onFailure = { AppNotice.error(requireActivity(), R.string.error_network) },
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        countView = null
        emptyView = null
        recycler = null
    }
}
