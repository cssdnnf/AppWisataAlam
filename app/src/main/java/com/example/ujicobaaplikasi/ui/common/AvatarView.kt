package com.example.ujicobaaplikasi.ui.common

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.example.ujicobaaplikasi.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * Avatar bulat dengan fallback inisial. Jika URI foto tidak valid atau izinnya
 * dicabut, komponen kembali menampilkan inisial tanpa membuat layar rusak.
 */
class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageView: ShapeableImageView
    private val initialsView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_avatar, this, true)
        imageView = findViewById(R.id.avatarImage)
        initialsView = findViewById(R.id.avatarInitials)

        val typed = context.obtainStyledAttributes(attrs, R.styleable.AvatarView)
        try {
            if (typed.hasValue(R.styleable.AvatarView_avatarInitialTextSize)) {
                initialsView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    typed.getDimension(R.styleable.AvatarView_avatarInitialTextSize, 0f),
                )
            }
        } finally {
            typed.recycle()
        }
    }

    fun setProfile(name: String?, imageUri: String?) {
        val safeName = name?.trim().orEmpty()
        initialsView.text = initialsFor(safeName)
        contentDescription = if (safeName.isBlank()) {
            context.getString(R.string.cd_avatar)
        } else {
            "${context.getString(R.string.cd_avatar)} $safeName"
        }

        if (imageUri.isNullOrBlank()) {
            showInitials()
            return
        }

        try {
            imageView.setImageURI(Uri.parse(imageUri))
            if (imageView.drawable == null) showInitials() else showImage()
        } catch (_: Exception) {
            showInitials()
        }
    }

    fun clearPhoto() {
        imageView.setImageDrawable(null)
        showInitials()
    }

    private fun showImage() {
        imageView.visibility = VISIBLE
        initialsView.visibility = GONE
    }

    private fun showInitials() {
        imageView.visibility = GONE
        initialsView.visibility = VISIBLE
    }

    private fun initialsFor(name: String): String {
        if (name.isBlank()) return "JL"
        val words = name.split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            words.size == 1 -> words.first().take(2).uppercase()
            else -> "${words.first().first()}${words.last().first()}".uppercase()
        }
    }
}
