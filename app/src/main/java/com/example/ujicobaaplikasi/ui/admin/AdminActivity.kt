package com.example.ujicobaaplikasi.ui.admin

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.data.AdminEntity
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.data.Catalog
import com.example.ujicobaaplikasi.ui.auth.LoginActivity
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = AuthStore.currentAccount(this)
        if (account == null || !account.isAdmin) {
            openLogin()
            return
        }

        setContentView(R.layout.activity_admin)
        findViewById<View>(R.id.adminHeader).applyStatusBarInset()
        findViewById<TextView>(R.id.adminName).text = account.name
        findViewById<TextView>(R.id.adminEmail).text =
            "${account.email} · ${getString(R.string.admin_greeting)}"

        val container = findViewById<LinearLayout>(R.id.adminMenuContainer)
        adminSections().forEach { section ->
            container.addView(adminRow(section.entity, section.iconKey, section.colorKey))
        }

        findViewById<View>(R.id.adminLogoutIcon).setOnClickListener { confirmLogout() }
        findViewById<View>(R.id.adminLogout).setOnClickListener { confirmLogout() }
        findViewById<View>(R.id.adminOpenApp).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private data class AdminSection(
        val entity: AdminEntity,
        val iconKey: String,
        val colorKey: String,
    )

    private fun adminSections() = listOf(
        AdminSection(AdminEntity.DESTINATION, "ic_menu_destination", "hijau_lombok"),
        AdminSection(AdminEntity.CULINARY, "ic_menu_culinary", "koral_gelap"),
        AdminSection(AdminEntity.LODGING, "ic_menu_stay", "lavender"),
        AdminSection(AdminEntity.ACTIVITY, "ic_menu_activity", "biru"),
        AdminSection(AdminEntity.EVENT, "ic_menu_event", "coklat_tua"),
        AdminSection(AdminEntity.NOTIFICATION, "ic_notifications", "hijau_lombok_gelap"),
        AdminSection(AdminEntity.USER, "ic_person", "biru"),
    )

    private fun adminRow(
        entity: AdminEntity,
        iconKey: String,
        colorKey: String,
    ): View {
        val density = resources.displayMetrics.density
        val card = MaterialCardView(this).apply {
            radius = 20 * density
            strokeWidth = (1 * density).toInt()
            strokeColor = ContextCompat.getColor(this@AdminActivity, R.color.garis_lembut)
            setCardBackgroundColor(ContextCompat.getColor(this@AdminActivity, R.color.permukaan))
            cardElevation = 0f
            isClickable = true
            isFocusable = true
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (14 * density).toInt(),
                (13 * density).toInt(),
                (14 * density).toInt(),
                (13 * density).toInt(),
            )
        }
        val iconTile = FrameLayout(this).apply {
            setBackgroundResource(R.drawable.bg_menu_icon_tile)
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@AdminActivity,
                    Catalog.color(
                        when (entity) {
                            AdminEntity.CULINARY -> "koral_lembut"
                            AdminEntity.LODGING -> "lavender_lembut"
                            AdminEntity.ACTIVITY, AdminEntity.USER -> "biru_lembut"
                            AdminEntity.EVENT -> "pasir_kuning"
                            else -> "hijau_lombok_muda"
                        },
                    ),
                ),
            )
        }
        val icon = ImageView(this).apply {
            setImageResource(Catalog.icon(iconKey))
            imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this@AdminActivity, Catalog.color(colorKey)),
            )
        }
        iconTile.addView(
            icon,
            FrameLayout.LayoutParams(
                (22 * density).toInt(),
                (22 * density).toInt(),
                Gravity.CENTER,
            ),
        )
        row.addView(
            iconTile,
            LinearLayout.LayoutParams(
                (44 * density).toInt(),
                (44 * density).toInt(),
            ),
        )

        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val title = TextView(this).apply {
            text = entity.label
            setTextColor(ContextCompat.getColor(this@AdminActivity, R.color.teks_utama))
            textSize = 14.5f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val subtitle = TextView(this).apply {
            text = getString(R.string.admin_manage_sub, entity.label)
            setTextColor(ContextCompat.getColor(this@AdminActivity, R.color.teks_sekunder))
            textSize = 11f
        }
        textColumn.addView(title)
        textColumn.addView(subtitle)
        row.addView(
            textColumn,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (12 * density).toInt()
            },
        )

        val chevron = ImageView(this).apply {
            setImageResource(R.drawable.ic_chevron_right)
            imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this@AdminActivity, R.color.teks_tersier),
            )
        }
        row.addView(
            chevron,
            LinearLayout.LayoutParams(
                (18 * density).toInt(),
                (18 * density).toInt(),
            ),
        )

        card.addView(row)
        card.setOnClickListener {
            AdminListActivity.open(this, entity)
        }
        return card.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = (9 * density).toInt() }
        }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.logout_confirm) { _, _ ->
                AuthStore.logout(this)
                openLogin()
            }
            .show()
    }

    private fun openLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }
}
