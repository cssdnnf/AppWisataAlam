package com.example.ujicobaaplikasi.ui.navigation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.ujicobaaplikasi.R

/**
 * Bottom navigation mandiri yang bisa dipakai ulang.
 *
 * Komponen ini tidak tahu Activity atau Fragment mana yang harus dibuka.
 * Host hanya perlu memasang listener dan mengatur [selectedDestination].
 * Navigasi bawah menangani inset sistem sendiri, jadi layout host tidak perlu
 * memakai fitsSystemWindows.
 */
class BottomNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val itemViews = mutableMapOf<BottomNavDestination, View>()
    private val iconViews = mutableMapOf<BottomNavDestination, ImageView>()
    private val labelViews = mutableMapOf<BottomNavDestination, TextView>()
    private var destinationListener: ((BottomNavDestination) -> Unit)? = null

    var selectedDestination: BottomNavDestination = BottomNavDestination.HOME
        set(value) {
            field = value
            renderSelection()
        }

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_bottom_navigation, this, true)

        BottomNavDestination.entries.forEach { destination ->
            val item = findViewById<View>(destination.itemId)
            val icon = item.findViewById<ImageView>(destination.iconId)
            val label = item.findViewById<TextView>(destination.labelId)
            icon.setImageResource(destination.iconRes)
            itemViews[destination] = item
            iconViews[destination] = icon
            labelViews[destination] = label
            item.contentDescription = context.getString(destination.labelRes)
            item.setOnClickListener {
                val wasSelected = selectedDestination == destination
                selectedDestination = destination
                if (!wasSelected) {
                    item.announceForAccessibility(context.getString(destination.labelRes))
                }
                // Reselect tetap dikirim agar layar detail dapat kembali ke shell utama.
                destinationListener?.invoke(destination)
            }
        }

        applyBottomSystemInset()
        renderSelection()
    }

    /** Memasang callback saat pengguna memilih tab lain. */
    fun setOnDestinationSelectedListener(
        listener: (BottomNavDestination) -> Unit,
    ) {
        destinationListener = listener
    }

    /** Berguna bila label ingin diubah dari kode tanpa mengedit string resource. */
    fun setLabel(destination: BottomNavDestination, text: CharSequence) {
        labelViews[destination]?.text = text
        itemViews[destination]?.contentDescription = text
    }

    private fun renderSelection() {
        BottomNavDestination.entries.forEach { destination ->
            val isSelected = destination == selectedDestination
            itemViews[destination]?.isSelected = isSelected
            iconViews[destination]?.isSelected = isSelected
            labelViews[destination]?.isSelected = isSelected
        }
    }

    private fun applyBottomSystemInset() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = bottomInset)
            insets
        }
        ViewCompat.requestApplyInsets(this)
    }
}
