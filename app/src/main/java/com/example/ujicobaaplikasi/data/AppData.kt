package com.example.ujicobaaplikasi.data

import android.content.Context
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet

/**
 * Cache konten per pengguna. Semua query berjalan di [Db], lalu hasilnya
 * dipublikasikan ke [TravelRepository] supaya layar dapat membaca data tanpa
 * memblokir main thread.
 */
object AppData {

    private data class Snapshot(
        val destinations: List<Destination>,
        val culinary: List<Culinary>,
        val lodging: List<Experience>,
        val activities: List<Experience>,
        val events: List<Experience>,
        val notifications: List<AppNotification>,
        val unreadNotifications: Set<String>,
        val saved: Map<SavedType, Set<String>>,
        val trips: List<TripPlan>,
    )

    @Volatile
    private var loadedUserId: Long? = null

    @Volatile
    private var loadingUserId: Long? = null

    @Volatile
    private var saved: Map<SavedType, Set<String>> = emptyMap()

    @Volatile
    private var unreadNotifications: Set<String> = emptySet()

    private val pendingCallbacks = mutableListOf<(Result<Unit>) -> Unit>()

    fun ensureLoaded(
        context: Context,
        force: Boolean = false,
        callback: (Result<Unit>) -> Unit,
    ) {
        val account = AuthStore.currentAccount(context)
        if (account == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }

        synchronized(this) {
            if (!force && loadedUserId == account.id) {
                callback(Result.success(Unit))
                return
            }
            if (loadingUserId == account.id) {
                pendingCallbacks.add(callback)
                return
            }
            loadingUserId = account.id
            pendingCallbacks.add(callback)
        }

        Db.async({ connection -> loadSnapshot(connection, account.id) }) { result ->
            val callbacks: List<(Result<Unit>) -> Unit>
            synchronized(this) {
                loadingUserId = null
                if (result.isSuccess) {
                    publish(result.getOrThrow(), account.id)
                }
                callbacks = pendingCallbacks.toList()
                pendingCallbacks.clear()
            }
            val outcome = result.map { Unit }
            callbacks.forEach { it(outcome) }
        }
    }

    fun isLoaded(context: Context): Boolean =
        loadedUserId != null && loadedUserId == AuthStore.currentUserId(context)

    fun savedIds(type: SavedType): Set<String> = saved[type].orEmpty()

    fun isSaved(type: SavedType, id: String): Boolean = id in savedIds(type)

    fun favoriteIds(): Set<String> = savedIds(SavedType.DESTINATION)

    fun isFavorite(destinationId: String): Boolean =
        isSaved(SavedType.DESTINATION, destinationId)

    fun hasUnreadNotifications(): Boolean = unreadNotifications.isNotEmpty()

    fun unreadNotificationIds(): Set<String> = unreadNotifications

    fun toggleFavorite(
        context: Context,
        destinationId: String,
        callback: (Result<Boolean>) -> Unit,
    ) = toggleSaved(context, SavedType.DESTINATION, destinationId, callback)

    fun toggleSaved(
        context: Context,
        type: SavedType,
        itemId: String,
        callback: (Result<Boolean>) -> Unit,
    ) {
        val userId = AuthStore.currentUserId(context)
        if (userId == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        val nowSaved = itemId !in savedIds(type)
        Db.async({ connection ->
            if (nowSaved) {
                connection.prepareStatement(
                    "INSERT IGNORE INTO ${type.table} (user_id, ${type.idColumn}) VALUES (?, ?)",
                ).use {
                    it.setLong(1, userId)
                    it.setString(2, itemId)
                    it.executeUpdate()
                }
            } else {
                connection.prepareStatement(
                    "DELETE FROM ${type.table} WHERE user_id = ? AND ${type.idColumn} = ?",
                ).use {
                    it.setLong(1, userId)
                    it.setString(2, itemId)
                    it.executeUpdate()
                }
            }
        }) { result ->
            if (result.isSuccess) {
                val current = savedIds(type).toMutableSet()
                if (nowSaved) current.add(itemId) else current.remove(itemId)
                saved = saved + (type to current)
            }
            callback(result.map { nowSaved })
        }
    }

    fun markAllNotificationsRead(context: Context, callback: (Result<Unit>) -> Unit) {
        val userId = AuthStore.currentUserId(context)
        if (userId == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        Db.async({ connection ->
            connection.prepareStatement(
                """INSERT IGNORE INTO notification_reads (user_id, notification_id)
                   SELECT ?, id FROM notifications""",
            ).use {
                it.setLong(1, userId)
                it.executeUpdate()
            }
        }) { result ->
            if (result.isSuccess) unreadNotifications = emptySet()
            callback(result.map { Unit })
        }
    }

    fun saveTrip(
        context: Context,
        tripId: Long?,
        title: String,
        startDate: String?,
        endDate: String?,
        notes: String,
        callback: (Result<Unit>) -> Unit,
    ) {
        val userId = AuthStore.currentUserId(context)
        if (userId == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        Db.async({ connection ->
            if (tripId == null) {
                connection.prepareStatement(
                    "INSERT INTO trips (user_id, title, start_date, end_date, notes) VALUES (?, ?, ?, ?, ?)",
                ).use {
                    it.setLong(1, userId)
                    it.setString(2, title)
                    it.setDate(3, startDate?.let(Date::valueOf))
                    it.setDate(4, endDate?.let(Date::valueOf))
                    it.setString(5, notes)
                    it.executeUpdate()
                }
            } else {
                connection.prepareStatement(
                    """UPDATE trips SET title = ?, start_date = ?, end_date = ?, notes = ?
                       WHERE id = ? AND user_id = ?""",
                ).use {
                    it.setString(1, title)
                    it.setDate(2, startDate?.let(Date::valueOf))
                    it.setDate(3, endDate?.let(Date::valueOf))
                    it.setString(4, notes)
                    it.setLong(5, tripId)
                    it.setLong(6, userId)
                    it.executeUpdate()
                }
            }
        }) { result ->
            if (result.isSuccess) refreshTrips(context, userId, callback) else callback(result.map { Unit })
        }
    }

    fun deleteTrip(context: Context, tripId: Long, callback: (Result<Unit>) -> Unit) {
        val userId = AuthStore.currentUserId(context)
        if (userId == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        Db.async({ connection ->
            connection.prepareStatement("DELETE FROM trips WHERE id = ? AND user_id = ?")
                .use {
                    it.setLong(1, tripId)
                    it.setLong(2, userId)
                    it.executeUpdate()
                }
        }) { result ->
            if (result.isSuccess) refreshTrips(context, userId, callback) else callback(result.map { Unit })
        }
    }

    fun saveTripItem(
        context: Context,
        tripId: Long,
        itemId: Long?,
        dayNumber: Int,
        timeLabel: String,
        title: String,
        notes: String,
        callback: (Result<Unit>) -> Unit,
    ) {
        val userId = AuthStore.currentUserId(context)
        if (userId == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        Db.async({ connection ->
            val ownsTrip = connection.prepareStatement(
                "SELECT 1 FROM trips WHERE id = ? AND user_id = ? LIMIT 1",
            ).use {
                it.setLong(1, tripId)
                it.setLong(2, userId)
                it.executeQuery().use { result -> result.next() }
            }
            if (!ownsTrip) throw IllegalStateException("Rencana trip tidak ditemukan.")

            if (itemId == null) {
                connection.prepareStatement(
                    """INSERT INTO trip_items (trip_id, day_number, time_label, title, notes, sort_order)
                       VALUES (?, ?, ?, ?, ?, ?)""",
                ).use {
                    it.setLong(1, tripId)
                    it.setInt(2, dayNumber)
                    it.setString(3, timeLabel)
                    it.setString(4, title)
                    it.setString(5, notes)
                    it.setInt(6, nextItemOrder(connection, tripId))
                    it.executeUpdate()
                }
            } else {
                connection.prepareStatement(
                    """UPDATE trip_items SET day_number = ?, time_label = ?, title = ?, notes = ?
                       WHERE id = ? AND trip_id = ?""",
                ).use {
                    it.setInt(1, dayNumber)
                    it.setString(2, timeLabel)
                    it.setString(3, title)
                    it.setString(4, notes)
                    it.setLong(5, itemId)
                    it.setLong(6, tripId)
                    it.executeUpdate()
                }
            }
        }) { result ->
            if (result.isSuccess) refreshTrips(context, userId, callback) else callback(result.map { Unit })
        }
    }

    fun deleteTripItem(
        context: Context,
        tripId: Long,
        itemId: Long,
        callback: (Result<Unit>) -> Unit,
    ) {
        val userId = AuthStore.currentUserId(context)
        if (userId == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        Db.async({ connection ->
            connection.prepareStatement(
                """DELETE ti FROM trip_items ti
                   JOIN trips t ON t.id = ti.trip_id
                   WHERE ti.id = ? AND ti.trip_id = ? AND t.user_id = ?""",
            ).use {
                it.setLong(1, itemId)
                it.setLong(2, tripId)
                it.setLong(3, userId)
                it.executeUpdate()
            }
        }) { result ->
            if (result.isSuccess) refreshTrips(context, userId, callback) else callback(result.map { Unit })
        }
    }

    fun refreshTrips(context: Context, callback: (Result<Unit>) -> Unit) {
        val userId = AuthStore.currentUserId(context)
        if (userId == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        refreshTrips(context, userId, callback)
    }

    private fun refreshTrips(
        context: Context,
        userId: Long,
        callback: (Result<Unit>) -> Unit,
    ) {
        Db.async({ connection -> loadTrips(connection, userId) }) { result ->
            result.onSuccess { TravelRepository.trips = it }
            callback(result.map { Unit })
        }
    }

    private fun nextItemOrder(connection: Connection, tripId: Long): Int {
        return connection.prepareStatement(
            "SELECT COALESCE(MAX(sort_order), 0) + 1 FROM trip_items WHERE trip_id = ?",
        ).use {
            it.setLong(1, tripId)
            it.executeQuery().use { result -> if (result.next()) result.getInt(1) else 1 }
        }
    }

    fun clear() {
        loadedUserId = null
        saved = emptyMap()
        unreadNotifications = emptySet()
        TravelRepository.clear()
    }

    private fun publish(snapshot: Snapshot, userId: Long) {
        loadedUserId = userId
        saved = snapshot.saved
        unreadNotifications = snapshot.unreadNotifications
        TravelRepository.apply(
            destinations = snapshot.destinations,
            culinary = snapshot.culinary,
            lodging = snapshot.lodging,
            activities = snapshot.activities,
            events = snapshot.events,
            notifications = snapshot.notifications,
            trips = snapshot.trips,
        )
    }

    private fun loadSnapshot(connection: Connection, userId: Long): Snapshot {
        val rawDestinations = connection.prepareStatement(
            "SELECT * FROM destinations ORDER BY sort_order, name",
        ).use { statement ->
            statement.executeQuery().use { result ->
                buildList { while (result.next()) add(mapDestination(result)) }
            }
        }
        val rawCulinary = connection.prepareStatement(
            "SELECT * FROM culinary ORDER BY sort_order, name",
        ).use { statement ->
            statement.executeQuery().use { result ->
                buildList { while (result.next()) add(mapCulinary(result)) }
            }
        }
        val rawLodging = loadExperiences(connection, "lodging", ExperienceType.LODGING)
        val rawActivities = loadExperiences(connection, "activities", ExperienceType.ACTIVITY)
        val rawEvents = loadExperiences(connection, "cultural_events", ExperienceType.EVENT)
        val mediaIds = buildSet {
            rawDestinations.mapNotNullTo(this) { it.imageMediaId }
            rawCulinary.mapNotNullTo(this) { it.imageMediaId }
            rawLodging.mapNotNullTo(this) { it.imageMediaId }
            rawActivities.mapNotNullTo(this) { it.imageMediaId }
            rawEvents.mapNotNullTo(this) { it.imageMediaId }
        }
        val mediaBytes = loadMediaBytes(connection, mediaIds)
        val destinations = rawDestinations.map { item ->
            item.copy(imageBytes = item.imageMediaId?.let(mediaBytes::get))
        }
        val culinary = rawCulinary.map { item ->
            item.copy(imageBytes = item.imageMediaId?.let(mediaBytes::get))
        }
        val lodging = rawLodging.map { item ->
            item.copy(imageBytes = item.imageMediaId?.let(mediaBytes::get))
        }
        val activities = rawActivities.map { item ->
            item.copy(imageBytes = item.imageMediaId?.let(mediaBytes::get))
        }
        val events = rawEvents.map { item ->
            item.copy(imageBytes = item.imageMediaId?.let(mediaBytes::get))
        }

        val notifications = mutableListOf<AppNotification>()
        val unread = mutableSetOf<String>()
        connection.prepareStatement(
            """SELECT n.*, (nr.notification_id IS NULL) AS unread
               FROM notifications n
               LEFT JOIN notification_reads nr
                 ON nr.notification_id = n.id AND nr.user_id = ?
               ORDER BY n.sort_order, n.created_at DESC""",
        ).use { statement ->
            statement.setLong(1, userId)
            statement.executeQuery().use { result ->
                while (result.next()) {
                    val notification = AppNotification(
                        id = result.getString("id"),
                        title = result.getString("title").orEmpty(),
                        message = result.getString("message").orEmpty(),
                        time = result.getString("time_label").orEmpty(),
                        iconRes = Catalog.icon(result.getString("icon_key")),
                        tintRes = Catalog.color(result.getString("tint_key")),
                        containerColorRes = Catalog.color(result.getString("container_key")),
                    )
                    notifications += notification
                    if (result.getInt("unread") == 1) unread += notification.id
                }
            }
        }

        val saved = SavedType.entries.associateWith { type ->
            connection.prepareStatement(
                "SELECT ${type.idColumn} FROM ${type.table} WHERE user_id = ?",
            ).use { statement ->
                statement.setLong(1, userId)
                statement.executeQuery().use { result ->
                    buildSet { while (result.next()) add(result.getString(1)) }
                }
            }
        }

        return Snapshot(
            destinations = destinations,
            culinary = culinary,
            lodging = lodging,
            activities = activities,
            events = events,
            notifications = notifications,
            unreadNotifications = unread,
            saved = saved,
            trips = loadTrips(connection, userId),
        )
    }

    private fun loadMediaBytes(connection: Connection, ids: Set<Long>): Map<Long, ByteArray> {
        if (ids.isEmpty()) return emptyMap()
        val placeholders = ids.joinToString(", ") { "?" }
        return connection.prepareStatement(
            "SELECT id, file_data FROM media_library WHERE id IN ($placeholders)",
        ).use { statement ->
            ids.forEachIndexed { index, id -> statement.setLong(index + 1, id) }
            statement.executeQuery().use { result ->
                buildMap {
                    while (result.next()) {
                        val bytes = result.getBytes("file_data") ?: continue
                        put(result.getLong("id"), bytes)
                    }
                }
            }
        }
    }

    private fun loadExperiences(
        connection: Connection,
        table: String,
        type: ExperienceType,
    ): List<Experience> {
        return connection.prepareStatement(
            "SELECT * FROM $table ORDER BY sort_order, title",
        ).use { statement ->
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        val mediaId = result.getLong("image_media_id")
                            .let { if (result.wasNull()) null else it }
                        add(
                            Experience(
                                id = result.getString("id"),
                                type = type,
                                title = result.getString("title").orEmpty(),
                                subtitle = result.getString("subtitle").orEmpty(),
                                category = result.getString("category").orEmpty(),
                                location = result.getString("location").orEmpty(),
                                price = result.getString("price").orEmpty(),
                                rating = result.getDouble("rating"),
                                reviewCount = result.getString("review_count").orEmpty(),
                                duration = result.getString("duration").orEmpty(),
                                openHours = result.getString("open_hours").orEmpty(),
                                imageRes = Catalog.image(result.getString("image_key")),
                                description = result.getString("description").orEmpty(),
                                highlights = TravelRepository.parseList(result.getString("highlights")),
                                facilities = TravelRepository.parseList(result.getString("facilities")),
                                bestTime = result.getString("best_time").orEmpty(),
                                mapQuery = result.getString("map_query").orEmpty(),
                                badge = result.getString("badge").orEmpty(),
                                iconRes = Catalog.icon(result.getString("icon_key")),
                                tintRes = Catalog.color(result.getString("tint_key")),
                                containerColorRes = Catalog.color(result.getString("container_key")),
                                imageMediaId = mediaId,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun loadTrips(connection: Connection, userId: Long): List<TripPlan> {
        val itemsByTrip = mutableMapOf<Long, MutableList<TripItem>>()
        connection.prepareStatement(
            """SELECT ti.* FROM trip_items ti
               JOIN trips t ON t.id = ti.trip_id
               WHERE t.user_id = ?
               ORDER BY ti.day_number, ti.sort_order, ti.id""",
        ).use { statement ->
            statement.setLong(1, userId)
            statement.executeQuery().use { result ->
                while (result.next()) {
                    val item = TripItem(
                        id = result.getLong("id"),
                        dayNumber = result.getInt("day_number"),
                        timeLabel = result.getString("time_label").orEmpty(),
                        title = result.getString("title").orEmpty(),
                        notes = result.getString("notes").orEmpty(),
                    )
                    itemsByTrip.getOrPut(result.getLong("trip_id")) { mutableListOf() } += item
                }
            }
        }

        return connection.prepareStatement(
            "SELECT * FROM trips WHERE user_id = ? ORDER BY start_date DESC, id DESC",
        ).use { statement ->
            statement.setLong(1, userId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        val id = result.getLong("id")
                        add(
                            TripPlan(
                                id = id,
                                title = result.getString("title").orEmpty(),
                                startDate = result.getDate("start_date")?.toString(),
                                endDate = result.getDate("end_date")?.toString(),
                                notes = result.getString("notes").orEmpty(),
                                items = itemsByTrip[id].orEmpty(),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun mapDestination(result: ResultSet): Destination {
        val mediaId = result.getLong("image_media_id").let { if (result.wasNull()) null else it }
        return Destination(
        id = result.getString("id"),
        name = result.getString("name").orEmpty(),
        location = result.getString("location").orEmpty(),
        category = result.getString("category").orEmpty(),
        rating = result.getDouble("rating"),
        reviewCount = result.getString("review_count").orEmpty(),
        price = result.getString("price").orEmpty(),
        duration = result.getString("duration").orEmpty(),
        openHours = result.getString("open_hours").orEmpty(),
        imageRes = Catalog.image(result.getString("image_key")),
        summary = result.getString("summary").orEmpty(),
        description = result.getString("description").orEmpty(),
        highlights = TravelRepository.parseList(result.getString("highlights")),
        facilities = TravelRepository.parseList(result.getString("facilities")),
        bestTime = result.getString("best_time").orEmpty(),
        mapQuery = result.getString("map_query").orEmpty(),
        imageMediaId = mediaId,
        )
    }

    private fun mapCulinary(result: ResultSet): Culinary {
        val mediaId = result.getLong("image_media_id").let { if (result.wasNull()) null else it }
        return Culinary(
        id = result.getString("id"),
        name = result.getString("name").orEmpty(),
        category = result.getString("category").orEmpty(),
        origin = result.getString("origin").orEmpty(),
        price = result.getString("price").orEmpty(),
        rating = result.getDouble("rating"),
        reviewCount = result.getString("review_count").orEmpty(),
        imageRes = Catalog.image(result.getString("image_key")),
        description = result.getString("description").orEmpty(),
        flavorTags = TravelRepository.parseList(result.getString("flavor_tags")),
        bestAt = result.getString("best_at").orEmpty(),
        tips = result.getString("tips").orEmpty(),
        relatedDestinationId = result.getString("related_destination_id").orEmpty(),
        imageMediaId = mediaId,
        )
    }
}
