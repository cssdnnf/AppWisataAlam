package com.example.ujicobaaplikasi.ui.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import kotlin.math.max

/** Menambahkan inset status bar ke padding atas yang sudah ada. */
fun View.applyStatusBarInset() {
    val initialTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.updatePadding(top = initialTop + top)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

/** Menambahkan inset navigation bar ke padding bawah yang sudah ada. */
fun View.applyNavigationBarInset() {
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        view.updatePadding(bottom = initialBottom + bottom)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

/** Untuk layar auth agar konten tetap bisa digulir saat IME terbuka. */
fun View.applyImeInset() {
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        view.updatePadding(bottom = initialBottom + max(bars, ime))
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
