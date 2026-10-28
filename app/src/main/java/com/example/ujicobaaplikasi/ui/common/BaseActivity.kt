package com.example.ujicobaaplikasi.ui.common

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Edge-to-edge untuk semua layar. Setiap layar yang memakai foto gelap di area
 * status bar dapat memanggil [useLightSystemIcons].
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.isNavigationBarContrastEnforced = false
        setStatusBarIcons(darkIcons = true)
        setNavigationBarIcons(darkIcons = true)
    }

    override fun onResume() {
        super.onResume()
        AppNotice.flush(this)
    }

    protected fun setStatusBarIcons(darkIcons: Boolean) {
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = darkIcons
    }

    protected fun setNavigationBarIcons(darkIcons: Boolean) {
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = darkIcons
    }

    protected fun useLightSystemIcons() {
        setStatusBarIcons(darkIcons = false)
        setNavigationBarIcons(darkIcons = false)
    }
}
