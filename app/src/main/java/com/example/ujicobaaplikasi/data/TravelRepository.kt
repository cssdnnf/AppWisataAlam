package com.example.ujicobaaplikasi.data

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.ujicobaaplikasi.R
import org.json.JSONArray

data class Destination(
    val id: String,
    val name: String,
    val location: String,
    val category: String,
    val rating: Double,
    val reviewCount: String,
    val price: String,
    val duration: String,
    val openHours: String,
    @DrawableRes val imageRes: Int,
    val summary: String,
    val description: String,
    val highlights: List<String>,
    val facilities: List<String>,
    val bestTime: String,
    val mapQuery: String,
    val imageBytes: ByteArray? = null,
    val imageMediaId: Long? = null,
)

data class Culinary(
    val id: String,
    val name: String,
    val category: String,
    val origin: String,
    val price: String,
    val rating: Double,
    val reviewCount: String,
    @DrawableRes val imageRes: Int,
    val description: String,
    val flavorTags: List<String>,
    val bestAt: String,
    val tips: String,
    val relatedDestinationId: String,
    val imageBytes: ByteArray? = null,
    val imageMediaId: Long? = null,
)

data class MenuEntry(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val pageSubtitleRes: Int,
    @DrawableRes val iconRes: Int,
    @ColorRes val iconTintRes: Int,
    @ColorRes val containerColorRes: Int,
)

enum class SavedType(
    val table: String,
    val idColumn: String,
    @StringRes val labelRes: Int,
) {
    DESTINATION("saved_destinations", "destination_id", R.string.menu_destination_title),
    CULINARY("saved_culinary", "culinary_id", R.string.menu_culinary_title),
    LODGING("saved_lodging", "lodging_id", R.string.menu_stay_title),
    ACTIVITY("saved_activities", "activity_id", R.string.menu_activity_title),
    EVENT("saved_events", "event_id", R.string.menu_event_title),
}

enum class ExperienceType(
    val menuId: String,
    @StringRes val labelRes: Int,
    val savedType: SavedType,
) {
    LODGING("stay", R.string.menu_stay_title, SavedType.LODGING),
    ACTIVITY("activity", R.string.menu_activity_title, SavedType.ACTIVITY),
    EVENT("event", R.string.menu_event_title, SavedType.EVENT),
}

data class Experience(
    val id: String,
    val type: ExperienceType,
    val title: String,
    val subtitle: String,
    val category: String,
    val location: String,
    val price: String,
    val rating: Double,
    val reviewCount: String,
    val duration: String,
    val openHours: String,
    @DrawableRes val imageRes: Int,
    val imageBytes: ByteArray? = null,
    val description: String,
    val highlights: List<String>,
    val facilities: List<String>,
    val bestTime: String,
    val mapQuery: String,
    val badge: String,
    @DrawableRes val iconRes: Int,
    @ColorRes val tintRes: Int,
    @ColorRes val containerColorRes: Int,
    val imageMediaId: Long? = null,
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    @DrawableRes val iconRes: Int,
    @ColorRes val tintRes: Int,
    @ColorRes val containerColorRes: Int,
)

data class TripItem(
    val id: Long,
    val dayNumber: Int,
    val timeLabel: String,
    val title: String,
    val notes: String,
)

data class TripPlan(
    val id: Long,
    val title: String,
    val startDate: String?,
    val endDate: String?,
    val notes: String,
    val items: List<TripItem> = emptyList(),
)

object TravelRepository {

    @Volatile
    var destinations: List<Destination> = emptyList()

    @Volatile
    var culinary: List<Culinary> = emptyList()

    @Volatile
    var lodging: List<Experience> = emptyList()

    @Volatile
    var activities: List<Experience> = emptyList()

    @Volatile
    var events: List<Experience> = emptyList()

    @Volatile
    var notifications: List<AppNotification> = emptyList()

    @Volatile
    var trips: List<TripPlan> = emptyList()

    val menus: List<MenuEntry> = listOf(
        MenuEntry(
            id = "destination",
            titleRes = R.string.menu_destination_title,
            subtitleRes = R.string.menu_destination_sub,
            pageSubtitleRes = R.string.menu_page_destination_sub,
            iconRes = R.drawable.ic_menu_destination,
            iconTintRes = R.color.hijau_lombok,
            containerColorRes = R.color.hijau_lombok_muda,
        ),
        MenuEntry(
            id = "culinary",
            titleRes = R.string.menu_culinary_title,
            subtitleRes = R.string.menu_culinary_sub,
            pageSubtitleRes = R.string.menu_page_culinary_sub,
            iconRes = R.drawable.ic_menu_culinary,
            iconTintRes = R.color.koral_gelap,
            containerColorRes = R.color.koral_lembut,
        ),
        MenuEntry(
            id = "stay",
            titleRes = R.string.menu_stay_title,
            subtitleRes = R.string.menu_stay_sub,
            pageSubtitleRes = R.string.menu_page_stay_sub,
            iconRes = R.drawable.ic_menu_stay,
            iconTintRes = R.color.lavender,
            containerColorRes = R.color.lavender_lembut,
        ),
        MenuEntry(
            id = "activity",
            titleRes = R.string.menu_activity_title,
            subtitleRes = R.string.menu_activity_sub,
            pageSubtitleRes = R.string.menu_page_activity_sub,
            iconRes = R.drawable.ic_menu_activity,
            iconTintRes = R.color.biru,
            containerColorRes = R.color.biru_lembut,
        ),
        MenuEntry(
            id = "event",
            titleRes = R.string.menu_event_title,
            subtitleRes = R.string.menu_event_sub,
            pageSubtitleRes = R.string.menu_page_event_sub,
            iconRes = R.drawable.ic_menu_event,
            iconTintRes = R.color.coklat_tua,
            containerColorRes = R.color.pasir_kuning,
        ),
        MenuEntry(
            id = "trip",
            titleRes = R.string.menu_trip_title,
            subtitleRes = R.string.menu_trip_sub,
            pageSubtitleRes = R.string.menu_page_trip_sub,
            iconRes = R.drawable.ic_menu_trip,
            iconTintRes = R.color.hijau_lombok_gelap,
            containerColorRes = R.color.hijau_lembut_ringan,
        ),
    )

    fun destinationById(id: String?): Destination? = destinations.firstOrNull { it.id == id }

    fun culinaryById(id: String?): Culinary? = culinary.firstOrNull { it.id == id }

    fun menuById(id: String?): MenuEntry? = menus.firstOrNull { it.id == id }

    fun experiencesForMenu(menuId: String): List<Experience> = when (menuId) {
        "stay" -> lodging
        "activity" -> activities
        "event" -> events
        else -> emptyList()
    }

    fun experienceById(type: ExperienceType, id: String?): Experience? =
        experiencesForMenu(type.menuId).firstOrNull { it.id == id }

    fun featuredDestinations(): List<Destination> = destinations.take(3)

    fun apply(
        destinations: List<Destination>,
        culinary: List<Culinary>,
        lodging: List<Experience>,
        activities: List<Experience>,
        events: List<Experience>,
        notifications: List<AppNotification>,
        trips: List<TripPlan>,
    ) {
        this.destinations = destinations
        this.culinary = culinary
        this.lodging = lodging
        this.activities = activities
        this.events = events
        this.notifications = notifications
        this.trips = trips
    }

    fun clear() {
        destinations = emptyList()
        culinary = emptyList()
        lodging = emptyList()
        activities = emptyList()
        events = emptyList()
        notifications = emptyList()
        trips = emptyList()
    }

    fun parseList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(value)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optString(index).trim()
                    if (item.isNotEmpty()) add(item)
                }
            }
        }.getOrElse {
            value.split(',', '\n').map(String::trim).filter(String::isNotEmpty)
        }
    }

    fun encodeList(items: List<String>): String {
        val array = JSONArray()
        items.filter(String::isNotBlank).map(String::trim).forEach(array::put)
        return array.toString()
    }
}
