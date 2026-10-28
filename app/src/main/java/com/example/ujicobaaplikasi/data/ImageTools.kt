package com.example.ujicobaaplikasi.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import android.util.LruCache
import android.widget.ImageView
import androidx.annotation.DrawableRes
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class PreparedImage(
    val name: String,
    val mime: String,
    val bytes: ByteArray,
    val thumbnail: ByteArray,
    val width: Int,
    val height: Int,
)

object ImageTools {

    private const val MAX_DIMENSION = 1600
    private const val THUMB_DIMENSION = 420
    private const val JPEG_QUALITY = 84

    private val cache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun bind(
        imageView: ImageView,
        @DrawableRes fallback: Int,
        bytes: ByteArray?,
        cacheKey: String,
    ) {
        if (bytes == null || bytes.isEmpty()) {
            imageView.setImageResource(fallback)
            return
        }
        val key = "$cacheKey:${bytes.size}:${System.identityHashCode(bytes)}"
        val bitmap = cache.get(key) ?: runCatching {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()?.also { cache.put(key, it) }
        if (bitmap != null) imageView.setImageBitmap(bitmap) else imageView.setImageResource(fallback)
    }

    fun bindThumbnail(imageView: ImageView, bytes: ByteArray?, fallback: Int) {
        if (bytes == null || bytes.isEmpty()) {
            imageView.setImageResource(fallback)
            return
        }
        val key = "thumb:${bytes.size}:${System.identityHashCode(bytes)}"
        val bitmap = cache.get(key) ?: runCatching {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()?.also { cache.put(key, it) }
        if (bitmap != null) imageView.setImageBitmap(bitmap) else imageView.setImageResource(fallback)
    }

    fun prepare(context: Context, uri: Uri): PreparedImage {
        val resolver = context.contentResolver
        val source = ImageDecoder.createSource(resolver, uri)
        val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longest = max(info.size.width, info.size.height)
            if (longest > MAX_DIMENSION) {
                decoder.setTargetSampleSize((longest / MAX_DIMENSION).coerceAtLeast(1))
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
        }

        val longest = max(decoded.width, decoded.height)
        val scaled = if (longest > MAX_DIMENSION) {
            val ratio = MAX_DIMENSION.toFloat() / longest
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * ratio).roundToInt().coerceAtLeast(1),
                (decoded.height * ratio).roundToInt().coerceAtLeast(1),
                true,
            ).also { if (it !== decoded) decoded.recycle() }
        } else {
            decoded
        }

        val fullBytes = scaled.toJpeg(JPEG_QUALITY)
        val thumb = scaled.let { bitmap ->
            val thumbLongest = max(bitmap.width, bitmap.height)
            if (thumbLongest > THUMB_DIMENSION) {
                val ratio = THUMB_DIMENSION.toFloat() / thumbLongest
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).roundToInt().coerceAtLeast(1),
                    (bitmap.height * ratio).roundToInt().coerceAtLeast(1),
                    true,
                )
            } else {
                bitmap
            }
        }
        val thumbBytes = thumb.toJpeg(78)
        if (thumb !== scaled) thumb.recycle()

        val displayName = resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
        val name = displayName?.substringBeforeLast('.')?.take(150)
            ?.ifBlank { null }
            ?: "gambar-${System.currentTimeMillis()}"
        val width = scaled.width
        val height = scaled.height
        scaled.recycle()

        return PreparedImage(
            name = name,
            mime = "image/jpeg",
            bytes = fullBytes,
            thumbnail = thumbBytes,
            width = width,
            height = height,
        )
    }

    private fun Bitmap.toJpeg(quality: Int): ByteArray {
        return ByteArrayOutputStream().use { output ->
            compress(Bitmap.CompressFormat.JPEG, quality, output)
            output.toByteArray()
        }
    }
}
