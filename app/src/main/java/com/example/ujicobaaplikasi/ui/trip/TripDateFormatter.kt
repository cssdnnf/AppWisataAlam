package com.example.ujicobaaplikasi.ui.trip

import java.text.SimpleDateFormat
import java.util.Locale

object TripDateFormatter {

    private val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val display = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))

    fun display(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return runCatching { display.format(iso.parse(value)!!) }.getOrDefault(value)
    }

    fun range(start: String?, end: String?): String {
        return when {
            start.isNullOrBlank() && end.isNullOrBlank() -> ""
            end.isNullOrBlank() -> display(start)
            start.isNullOrBlank() -> display(end)
            start == end -> display(start)
            else -> "${display(start)} – ${display(end)}"
        }
    }
}
