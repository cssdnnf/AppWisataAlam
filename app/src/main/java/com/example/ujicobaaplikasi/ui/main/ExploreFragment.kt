package com.example.ujicobaaplikasi.ui.main

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.Destination
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.adapters.DestinationAdapter
import com.example.ujicobaaplikasi.ui.detail.DestinationDetailActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private var adapter: DestinationAdapter? = null
    private var resultCount: TextView? = null
    private var emptyState: View? = null
    private var query: String = ""
    private var selectedCategory: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultCount = view.findViewById(R.id.textHasilCari)
        emptyState = view.findViewById(R.id.emptySearch)

        val recycler = view.findViewById<RecyclerView>(R.id.exploreRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.itemAnimator = null
        adapter = DestinationAdapter(
            onOpen = { destination ->
                DestinationDetailActivity.open(
                    requireContext(),
                    destination.id,
                    BottomNavDestination.EXPLORE,
                )
            },
            onFavoriteToggle = ::toggleFavorite,
        )
        recycler.adapter = adapter

        val search = view.findViewById<TextInputEditText>(R.id.etCariDestinasi)
        search.doAfterTextChanged {
            query = it?.toString()?.trim().orEmpty()
            applyFilters()
        }

        val chips = view.findViewById<ChipGroup>(R.id.filterChips)
        chips.setOnCheckedStateChangeListener { group, checkedIds ->
            val checked = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            selectedCategory = when (checked?.id) {
                R.id.chipGunung -> "Gunung"
                R.id.chipPantai -> "Pantai"
                R.id.chipPulau -> "Pulau"
                R.id.chipAirTerjun -> "Air terjun"
                R.id.chipBudaya -> "Budaya"
                R.id.chipBukit -> "Bukit"
                else -> null
            }
            applyFilters()
        }

        view.findViewById<View>(R.id.tombolResetFilter).setOnClickListener {
            query = ""
            selectedCategory = null
            search.setText("")
            chips.check(R.id.chipSemua)
            applyFilters()
        }

        applyFilters()
    }

    override fun onResume() {
        super.onResume()
        adapter?.updateFavorites(AppData.favoriteIds())
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            adapter?.updateFavorites(AppData.favoriteIds())
        }
    }

    private fun applyFilters() {
        val filtered = TravelRepository.destinations.filter { destination ->
            val matchesQuery = query.isBlank() ||
                destination.name.contains(query, ignoreCase = true) ||
                destination.location.contains(query, ignoreCase = true) ||
                destination.category.contains(query, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                destination.category.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }

        adapter?.submitList(filtered, AppData.favoriteIds())
        resultCount?.text = getString(R.string.format_search_result, filtered.size)
        emptyState?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        resultCount?.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun toggleFavorite(destination: Destination) {
        AppData.toggleFavorite(requireContext(), destination.id) { result ->
            result.fold(
                onSuccess = { nowFavorite ->
                    adapter?.updateFavorites(AppData.favoriteIds())
                    if (nowFavorite) {
                        AppNotice.success(requireActivity(), R.string.favorite_added)
                    } else {
                        AppNotice.info(requireActivity(), R.string.favorite_removed)
                    }
                },
                onFailure = {
                    AppNotice.error(requireActivity(), R.string.error_network)
                },
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        resultCount = null
        emptyState = null
    }
}
