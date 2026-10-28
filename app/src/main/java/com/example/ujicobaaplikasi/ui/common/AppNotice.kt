package com.example.ujicobaaplikasi.ui.common

import android.app.Activity
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ujicobaaplikasi.R
import com.google.android.material.card.MaterialCardView

/**
 * Banner notifikasi in-app. Ditampilkan di atas konten, bukan Toast sistem,
 * sehingga tidak menempel ke navigation bar dan tetap konsisten di semua layar.
 *
 * Gunakan [defer] ketika layar akan langsung ditutup, agar pesan muncul di
 * layar berikutnya. [flush] dipanggil otomatis oleh BaseActivity.onResume().
 */
object AppNotice {

    private const val TAG = "jelajah_app_notice"

    private enum class Kind { SUCCESS, ERROR, INFO }

    private data class Message(val text: CharSequence, val kind: Kind)

    private var pending: Message? = null

    fun success(activity: Activity, @StringRes messageRes: Int) {
        show(activity, activity.getString(messageRes), Kind.SUCCESS)
    }

    fun success(activity: Activity, message: CharSequence) {
        show(activity, message, Kind.SUCCESS)
    }

    fun error(activity: Activity, @StringRes messageRes: Int) {
        show(activity, activity.getString(messageRes), Kind.ERROR)
    }

    fun error(activity: Activity, message: CharSequence) {
        show(activity, message, Kind.ERROR)
    }

    fun info(activity: Activity, @StringRes messageRes: Int) {
        show(activity, activity.getString(messageRes), Kind.INFO)
    }

    fun info(activity: Activity, message: CharSequence) {
        show(activity, message, Kind.INFO)
    }

    fun deferSuccess(activity: Activity, @StringRes messageRes: Int) {
        pending = Message(activity.getString(messageRes), Kind.SUCCESS)
    }

    fun deferSuccess(activity: Activity, message: CharSequence) {
        pending = Message(message, Kind.SUCCESS)
    }

    fun deferError(activity: Activity, @StringRes messageRes: Int) {
        pending = Message(activity.getString(messageRes), Kind.ERROR)
    }

    fun deferInfo(activity: Activity, @StringRes messageRes: Int) {
        pending = Message(activity.getString(messageRes), Kind.INFO)
    }

    fun flush(activity: Activity) {
        val message = pending ?: return
        pending = null
        activity.window.decorView.post {
            if (activity.isFinishing || activity.isDestroyed) {
                pending = message
            } else {
                show(activity, message.text, message.kind)
            }
        }
    }

    private fun show(activity: Activity, message: CharSequence, kind: Kind) {
        if (message.isBlank()) return
        if (activity.isFinishing || activity.isDestroyed) {
            pending = Message(message, kind)
            return
        }
        val root = activity.findViewById<ViewGroup>(android.R.id.content) as? FrameLayout
        if (root == null) {
            pending = Message(message, kind)
            return
        }

        root.findViewWithTag<View>(TAG)?.let(root::removeView)

        val notice = LayoutInflater.from(activity)
            .inflate(R.layout.view_app_notice, root, false)
        val card = notice as MaterialCardView
        val iconContainer = notice.findViewById<View>(R.id.appNoticeIconContainer)
        val icon = notice.findViewById<ImageView>(R.id.appNoticeIcon)
        val title = notice.findViewById<TextView>(R.id.appNoticeTitle)
        val messageView = notice.findViewById<TextView>(R.id.appNoticeMessage)
        val close = notice.findViewById<View>(R.id.appNoticeClose)

        val accentRes = when (kind) {
            Kind.SUCCESS -> R.color.hijau_lombok
            Kind.ERROR -> R.color.merah
            Kind.INFO -> R.color.biru
        }
        val containerRes = when (kind) {
            Kind.SUCCESS -> R.color.hijau_lombok_muda
            Kind.ERROR -> R.color.merah_lembut
            Kind.INFO -> R.color.biru_lembut
        }
        val iconRes = when (kind) {
            Kind.SUCCESS -> R.drawable.ic_check
            Kind.ERROR -> R.drawable.ic_warning
            Kind.INFO -> R.drawable.ic_info
        }
        val titleRes = when (kind) {
            Kind.SUCCESS -> R.string.notice_success_title
            Kind.ERROR -> R.string.notice_error_title
            Kind.INFO -> R.string.notice_info_title
        }

        val accent = ContextCompat.getColor(activity, accentRes)
        iconContainer.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(activity, containerRes),
        )
        icon.setImageResource(iconRes)
        icon.imageTintList = ColorStateList.valueOf(accent)
        title.text = activity.getString(titleRes)
        title.setTextColor(accent)
        messageView.text = message
        card.strokeColor = ContextCompat.getColor(activity, containerRes)
        notice.contentDescription = "${title.text}. $message"

        val density = activity.resources.displayMetrics.density
        val topInset = runCatching {
            ViewCompat.getRootWindowInsets(activity.window.decorView)
                ?.getInsets(WindowInsetsCompat.Type.statusBars())?.top
        }.getOrNull() ?: 0
        val fallbackTop = runCatching {
            val id = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (id > 0) activity.resources.getDimensionPixelSize(id) else 0
        }.getOrDefault(0)
        val margin = (16 * density).toInt()
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            gravity = Gravity.TOP
            setMargins(margin, (if (topInset > 0) topInset else fallbackTop) + (8 * density).toInt(), margin, 0)
        }

        notice.alpha = 0f
        notice.translationY = (-18 * density)
        root.addView(notice, params)
        notice.tag = TAG
        ViewCompat.setAccessibilityLiveRegion(
            notice,
            View.ACCESSIBILITY_LIVE_REGION_POLITE,
        )
        notice.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220)
            .setInterpolator(DecelerateInterpolator())
            .start()

        val dismiss = Runnable { dismiss(notice, root) }
        notice.setTag(R.id.appNoticeCard, dismiss)
        notice.postDelayed(dismiss, if (kind == Kind.ERROR) 4500L else 3200L)
        notice.setOnClickListener { dismiss(notice, root) }
        close.setOnClickListener { dismiss(notice, root) }
    }

    private fun dismiss(notice: View, root: FrameLayout) {
        if (notice.parent !== root) return
        (notice.getTag(R.id.appNoticeCard) as? Runnable)?.let(notice::removeCallbacks)
        notice.animate()
            .alpha(0f)
            .translationY(-12f)
            .setDuration(160)
            .withEndAction {
                (notice.parent as? ViewGroup)?.removeView(notice)
            }
            .start()
    }
}
