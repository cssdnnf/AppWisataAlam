package com.example.ujicobaaplikasi.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.example.ujicobaaplikasi.R

/**
 * Empat tujuan utama aplikasi. Tambahkan atau ganti item di sini dan di
 * res/layout/view_bottom_navigation.xml saat memakai template ini di proyek lain.
 */
enum class BottomNavDestination(
    @IdRes val itemId: Int,
    @IdRes val iconId: Int,
    @IdRes val labelId: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
) {
    HOME(
        itemId = R.id.navItemHome,
        iconId = R.id.navIconHome,
        labelId = R.id.navLabelHome,
        iconRes = R.drawable.ic_nav_home,
        labelRes = R.string.nav_home,
    ),
    EXPLORE(
        itemId = R.id.navItemExplore,
        iconId = R.id.navIconExplore,
        labelId = R.id.navLabelExplore,
        iconRes = R.drawable.ic_nav_explore,
        labelRes = R.string.nav_explore,
    ),
    SAVED(
        itemId = R.id.navItemSaved,
        iconId = R.id.navIconSaved,
        labelId = R.id.navLabelSaved,
        iconRes = R.drawable.ic_nav_saved,
        labelRes = R.string.nav_saved,
    ),
    PROFILE(
        itemId = R.id.navItemProfile,
        iconId = R.id.navIconProfile,
        labelId = R.id.navLabelProfile,
        iconRes = R.drawable.ic_nav_profile,
        labelRes = R.string.nav_profile,
    ),
}
