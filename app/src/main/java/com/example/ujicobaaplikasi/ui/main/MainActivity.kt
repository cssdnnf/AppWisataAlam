package com.example.ujicobaaplikasi.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.ui.auth.LoginActivity
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.navigation.BottomNavView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : BaseActivity() {

    private lateinit var bottomNav: BottomNavView
    private lateinit var loading: View
    private val fragmentCache = mutableMapOf<BottomNavDestination, Fragment>()
    private var currentTab = BottomNavDestination.HOME
    private var contentReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthStore.isLoggedIn(this)) {
            openLogin()
            return
        }

        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.mainContent).applyStatusBarInset()
        bottomNav = findViewById(R.id.bottomNav)
        loading = findViewById(R.id.mainLoading)
        bottomNav.setOnDestinationSelectedListener { destination ->
            if (destination != currentTab) showTab(destination)
        }

        val requestedTab = runCatching {
            BottomNavDestination.valueOf(intent.getStringExtra(EXTRA_TAB).orEmpty())
        }.getOrDefault(BottomNavDestination.HOME)
        loadContent(requestedTab)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (currentTab != BottomNavDestination.HOME) {
                        showTab(BottomNavDestination.HOME)
                    } else {
                        finish()
                    }
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        if (contentReady && !AppData.isLoaded(this)) {
            contentReady = false
            loadContent(currentTab, force = true)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val requestedTab = runCatching {
            BottomNavDestination.valueOf(intent.getStringExtra(EXTRA_TAB).orEmpty())
        }.getOrDefault(currentTab)
        if (contentReady && requestedTab != currentTab) showTab(requestedTab)
    }

    fun showTab(destination: BottomNavDestination) {
        if (!contentReady) return
        val target = fragmentCache.getOrPut(destination) { createFragment(destination) }
        supportFragmentManager.commitNow {
            supportFragmentManager.fragments.forEach { fragment -> hide(fragment) }
            if (target.isAdded) {
                show(target)
            } else {
                add(R.id.mainContent, target, destination.name)
            }
        }
        currentTab = destination
        bottomNav.selectedDestination = destination
    }

    private fun loadContent(requestedTab: BottomNavDestination, force: Boolean = false) {
        loading.visibility = View.VISIBLE
        bottomNav.visibility = View.INVISIBLE
        AppData.ensureLoaded(this, force = force) { result ->
            if (result.isSuccess) {
                contentReady = true
                loading.visibility = View.GONE
                bottomNav.visibility = View.VISIBLE
                if (!force) {
                    showTab(requestedTab)
                } else {
                    resetFragments()
                    showTab(requestedTab)
                }
            } else {
                loading.visibility = View.GONE
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.error_loading_data)
                    .setMessage(result.exceptionOrNull()?.message ?: getString(R.string.error_network))
                    .setCancelable(false)
                    .setNegativeButton(R.string.logout_confirm) { _, _ ->
                        AuthStore.logout(this)
                        openLogin()
                    }
                    .setPositiveButton(R.string.action_retry) { _, _ ->
                        loadContent(requestedTab, force = true)
                    }
                    .show()
            }
        }
    }

    private fun resetFragments() {
        supportFragmentManager.commitNow {
            supportFragmentManager.fragments.forEach { remove(it) }
        }
        fragmentCache.clear()
        bottomNav.selectedDestination = currentTab
    }

    private fun createFragment(destination: BottomNavDestination): Fragment = when (destination) {
        BottomNavDestination.HOME -> HomeFragment()
        BottomNavDestination.EXPLORE -> ExploreFragment()
        BottomNavDestination.SAVED -> SavedFragment()
        BottomNavDestination.PROFILE -> ProfileFragment()
    }

    private fun openLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }

    companion object {
        private const val EXTRA_TAB = "extra_tab"

        /** Kembali ke shell utama dan sinkronkan tab yang dipilih. */
        fun open(context: Context, destination: BottomNavDestination) {
            context.startActivity(
                Intent(context, MainActivity::class.java).apply {
                    putExtra(EXTRA_TAB, destination.name)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
            )
        }
    }
}
