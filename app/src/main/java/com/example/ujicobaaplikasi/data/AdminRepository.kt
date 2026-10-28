package com.example.ujicobaaplikasi.data

import java.sql.Connection

enum class AdminFieldType {
    TEXT,
    MULTILINE,
    LIST,
    DECIMAL,
    INTEGER,
    IMAGE,
    ICON,
    COLOR,
    DESTINATION,
    ROLE,
    PASSWORD,
}

data class AdminField(
    val column: String,
    val label: String,
    val type: AdminFieldType,
    val required: Boolean = false,
    val hint: String = "",
)

private fun experienceFields(nameLabel: String) = listOf(
    AdminField("title", nameLabel, AdminFieldType.TEXT, required = true),
    AdminField("subtitle", "Ringkasan singkat", AdminFieldType.TEXT, required = true),
    AdminField("category", "Kategori", AdminFieldType.TEXT, required = true),
    AdminField("location", "Lokasi", AdminFieldType.TEXT, required = true),
    AdminField("price", "Harga", AdminFieldType.TEXT),
    AdminField("rating", "Rating", AdminFieldType.DECIMAL, hint = "Nilai 0 sampai 5"),
    AdminField("review_count", "Jumlah ulasan", AdminFieldType.TEXT),
    AdminField("duration", "Durasi", AdminFieldType.TEXT),
    AdminField("open_hours", "Jadwal / jam buka", AdminFieldType.TEXT),
    AdminField("image_key", "Gambar konten", AdminFieldType.IMAGE, hint = "Pilih gambar bawaan atau unggah gambar baru"),
    AdminField("description", "Deskripsi", AdminFieldType.MULTILINE),
    AdminField("highlights", "Highlight", AdminFieldType.LIST, hint = "Pisahkan dengan koma"),
    AdminField("facilities", "Fasilitas", AdminFieldType.LIST, hint = "Pisahkan dengan koma"),
    AdminField("best_time", "Waktu terbaik", AdminFieldType.TEXT),
    AdminField("map_query", "Pencarian Maps", AdminFieldType.TEXT),
    AdminField("badge", "Badge singkat", AdminFieldType.TEXT, hint = "Contoh: Rp650.000/malam atau Feb–Mar"),
    AdminField("icon_key", "Ikon", AdminFieldType.ICON),
    AdminField("tint_key", "Warna ikon", AdminFieldType.COLOR),
    AdminField("container_key", "Warna latar ikon", AdminFieldType.COLOR),
    AdminField("sort_order", "Urutan tampil", AdminFieldType.INTEGER, hint = "Angka kecil tampil lebih dahulu"),
)

enum class AdminEntity(
    val key: String,
    val label: String,
    val table: String,
    val idColumn: String,
    val numericKey: Boolean,
    val titleColumn: String,
    val subtitleColumns: List<String>,
    val orderBy: String,
    val fields: List<AdminField>,
) {
    DESTINATION(
        key = "destination",
        label = "Destinasi",
        table = "destinations",
        idColumn = "id",
        numericKey = false,
        titleColumn = "name",
        subtitleColumns = listOf("location", "category"),
        orderBy = "sort_order, name",
        fields = listOf(
            AdminField("name", "Nama destinasi", AdminFieldType.TEXT, required = true),
            AdminField("location", "Lokasi", AdminFieldType.TEXT, required = true),
            AdminField("category", "Kategori", AdminFieldType.TEXT, required = true),
            AdminField("rating", "Rating", AdminFieldType.DECIMAL, hint = "Nilai 0 sampai 5"),
            AdminField("review_count", "Jumlah ulasan", AdminFieldType.TEXT),
            AdminField("price", "Harga tiket", AdminFieldType.TEXT),
            AdminField("duration", "Durasi", AdminFieldType.TEXT),
            AdminField("open_hours", "Jam buka", AdminFieldType.TEXT),
            AdminField("image_key", "Gambar konten", AdminFieldType.IMAGE, hint = "Pilih gambar bawaan atau unggah gambar baru"),
            AdminField("summary", "Ringkasan", AdminFieldType.MULTILINE),
            AdminField("description", "Deskripsi", AdminFieldType.MULTILINE),
            AdminField("highlights", "Highlight", AdminFieldType.LIST, hint = "Pisahkan dengan koma"),
            AdminField("facilities", "Fasilitas", AdminFieldType.LIST, hint = "Pisahkan dengan koma"),
            AdminField("best_time", "Waktu terbaik", AdminFieldType.TEXT),
            AdminField("map_query", "Pencarian Maps", AdminFieldType.TEXT),
            AdminField("sort_order", "Urutan tampil", AdminFieldType.INTEGER, hint = "Angka kecil tampil lebih dahulu"),
        ),
    ),
    CULINARY(
        key = "culinary",
        label = "Kuliner",
        table = "culinary",
        idColumn = "id",
        numericKey = false,
        titleColumn = "name",
        subtitleColumns = listOf("origin", "price"),
        orderBy = "sort_order, name",
        fields = listOf(
            AdminField("name", "Nama kuliner", AdminFieldType.TEXT, required = true),
            AdminField("category", "Kategori", AdminFieldType.TEXT, required = true),
            AdminField("origin", "Asal", AdminFieldType.TEXT, required = true),
            AdminField("price", "Kisaran harga", AdminFieldType.TEXT),
            AdminField("rating", "Rating", AdminFieldType.DECIMAL, hint = "Nilai 0 sampai 5"),
            AdminField("review_count", "Jumlah ulasan", AdminFieldType.TEXT),
            AdminField("image_key", "Gambar konten", AdminFieldType.IMAGE, hint = "Pilih gambar bawaan atau unggah gambar baru"),
            AdminField("description", "Deskripsi", AdminFieldType.MULTILINE),
            AdminField("flavor_tags", "Cita rasa", AdminFieldType.LIST, hint = "Pisahkan dengan koma"),
            AdminField("best_at", "Paling nikmat disantap", AdminFieldType.TEXT),
            AdminField("tips", "Tips", AdminFieldType.MULTILINE),
            AdminField("related_destination_id", "Destinasi terkait", AdminFieldType.DESTINATION, hint = "Boleh dikosongkan"),
            AdminField("sort_order", "Urutan tampil", AdminFieldType.INTEGER, hint = "Angka kecil tampil lebih dahulu"),
        ),
    ),
    LODGING(
        key = "lodging",
        label = "Penginapan",
        table = "lodging",
        idColumn = "id",
        numericKey = false,
        titleColumn = "title",
        subtitleColumns = listOf("subtitle"),
        orderBy = "sort_order, title",
        fields = experienceFields("Nama penginapan"),
    ),
    ACTIVITY(
        key = "activity",
        label = "Aktivitas",
        table = "activities",
        idColumn = "id",
        numericKey = false,
        titleColumn = "title",
        subtitleColumns = listOf("subtitle"),
        orderBy = "sort_order, title",
        fields = experienceFields("Nama aktivitas"),
    ),
    EVENT(
        key = "event",
        label = "Event Budaya",
        table = "cultural_events",
        idColumn = "id",
        numericKey = false,
        titleColumn = "title",
        subtitleColumns = listOf("subtitle"),
        orderBy = "sort_order, title",
        fields = experienceFields("Nama event"),
    ),
    NOTIFICATION(
        key = "notification",
        label = "Notifikasi",
        table = "notifications",
        idColumn = "id",
        numericKey = false,
        titleColumn = "title",
        subtitleColumns = listOf("time_label"),
        orderBy = "sort_order, created_at DESC",
        fields = listOf(
            AdminField("title", "Judul notifikasi", AdminFieldType.TEXT, required = true),
            AdminField("message", "Pesan", AdminFieldType.MULTILINE, required = true),
            AdminField("time_label", "Label waktu", AdminFieldType.TEXT, hint = "Contoh: Baru saja"),
            AdminField("icon_key", "Ikon", AdminFieldType.ICON),
            AdminField("tint_key", "Warna ikon", AdminFieldType.COLOR),
            AdminField("container_key", "Warna latar ikon", AdminFieldType.COLOR),
            AdminField("sort_order", "Urutan tampil", AdminFieldType.INTEGER, hint = "Angka kecil tampil lebih dahulu"),
        ),
    ),
    USER(
        key = "user",
        label = "Pengguna",
        table = "users",
        idColumn = "id",
        numericKey = true,
        titleColumn = "name",
        subtitleColumns = listOf("email", "role"),
        orderBy = "name",
        fields = listOf(
            AdminField("name", "Nama", AdminFieldType.TEXT, required = true),
            AdminField("email", "Email", AdminFieldType.TEXT, required = true),
            AdminField("phone", "Telepon", AdminFieldType.TEXT),
            AdminField("city", "Kota", AdminFieldType.TEXT),
            AdminField("bio", "Bio", AdminFieldType.MULTILINE),
            AdminField("role", "Role", AdminFieldType.ROLE),
            AdminField("password", "Kata sandi baru", AdminFieldType.PASSWORD,
                hint = "Kosongkan bila tidak ingin mengubah"),
        ),
    ),
}

data class AdminRecord(val id: String, val title: String, val subtitle: String)

data class AdminOption(val value: String, val label: String)

object AdminRepository {

    fun list(entity: AdminEntity, callback: (Result<List<AdminRecord>>) -> Unit) {
        val subtitle = entity.subtitleColumns.joinToString(", ") { "COALESCE($it, '')" }
        val sql = """SELECT ${entity.idColumn} AS record_id,
                            ${entity.titleColumn} AS list_title,
                            CONCAT_WS(' · ', $subtitle) AS list_subtitle
                     FROM ${entity.table}
                     ORDER BY ${entity.orderBy}"""
        Db.async({ connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                AdminRecord(
                                    id = result.getString("record_id"),
                                    title = result.getString("list_title").orEmpty(),
                                    subtitle = result.getString("list_subtitle").orEmpty(),
                                ),
                            )
                        }
                    }
                }
            }
        }, callback)
    }

    fun load(
        entity: AdminEntity,
        id: String,
        callback: (Result<Map<String, String>>) -> Unit,
    ) {
        Db.async({ connection ->
            connection.prepareStatement(
                "SELECT * FROM ${entity.table} WHERE ${entity.idColumn} = ? LIMIT 1",
            ).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { result ->
                    if (!result.next()) error("Data tidak ditemukan.")
                    buildMap {
                        entity.fields.forEach { field ->
                            if (field.type != AdminFieldType.PASSWORD) {
                                put(field.column, result.getString(field.column).orEmpty())
                            }
                        }
                        if (entity.fields.any { it.type == AdminFieldType.IMAGE }) {
                            val mediaId = result.getLong("image_media_id")
                                .let { if (result.wasNull()) null else it }
                            val builtIn = result.getString("image_key").orEmpty()
                            put(
                                "image_key",
                                when {
                                    mediaId != null -> MediaRepository.keyFor(mediaId)
                                    builtIn.isNotBlank() -> "builtin:$builtIn"
                                    else -> ""
                                },
                            )
                        }
                    }
                }
            }
        }, callback)
    }

    fun save(
        entity: AdminEntity,
        id: String?,
        values: Map<String, String>,
        callback: (Result<String>) -> Unit,
    ) {
        Db.async({ connection ->
            connection.autoCommit = false
            try {
                val savedId = if (entity == AdminEntity.USER) {
                    saveUser(connection, id, values)
                } else {
                    saveContent(connection, entity, id, values)
                }
                connection.commit()
                savedId
            } catch (throwable: Throwable) {
                runCatching { connection.rollback() }
                throw throwable
            } finally {
                connection.autoCommit = true
            }
        }) { result ->
            callback(result)
        }
    }

    fun delete(
        entity: AdminEntity,
        id: String,
        currentUserId: Long,
        callback: (Result<Unit>) -> Unit,
    ) {
        Db.async({ connection ->
            if (entity == AdminEntity.USER) {
                val targetId = id.toLongOrNull() ?: error("ID pengguna tidak valid.")
                if (targetId == currentUserId) error("Akun yang sedang dipakai tidak bisa dihapus.")
                val isAdmin = connection.prepareStatement(
                    "SELECT role FROM users WHERE id = ?",
                ).use {
                    it.setLong(1, targetId)
                    it.executeQuery().use { result ->
                        if (result.next()) result.getString("role") else null
                    }
                }
                if (isAdmin == "admin") {
                    val adminCount = connection.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE role = 'admin'",
                    ).use { statement ->
                        statement.executeQuery().use { result ->
                            if (result.next()) result.getInt(1) else 0
                        }
                    }
                    if (adminCount <= 1) error("Minimal satu akun admin harus tersisa.")
                }
            }
            connection.prepareStatement(
                "DELETE FROM ${entity.table} WHERE ${entity.idColumn} = ?",
            ).use {
                it.setString(1, id)
                it.executeUpdate()
            }
        }, callback)
    }

    fun destinationOptions(callback: (Result<List<AdminOption>>) -> Unit) {
        Db.async({ connection ->
            connection.prepareStatement("SELECT id, name FROM destinations ORDER BY name")
                .use { statement ->
                    statement.executeQuery().use { result ->
                        buildList {
                            while (result.next()) {
                                add(AdminOption(result.getString("id"), result.getString("name")))
                            }
                        }
                    }
                }
        }, callback)
    }

    private fun saveContent(
        connection: Connection,
        entity: AdminEntity,
        id: String?,
        values: Map<String, String>,
    ): String {
        val fields = entity.fields
        val hasImage = fields.any { it.type == AdminFieldType.IMAGE }
        val imageValue = values["image_key"].orEmpty()
        val imageKey = MediaRepository.parseBuiltInKey(imageValue).orEmpty()
        val imageMediaId = MediaRepository.parseMediaId(imageValue)
        val storedValues = if (hasImage) {
            values.toMutableMap().apply { put("image_key", imageKey) }
        } else {
            values
        }
        val columns = fields.map { it.column } + if (hasImage) listOf("image_media_id") else emptyList()

        if (id == null) {
            val key = uniqueSlug(connection, entity, values[entity.titleColumn].orEmpty())
            val placeholders = (listOf(entity.idColumn) + columns).joinToString(", ") { "?" }
            val sql = "INSERT INTO ${entity.table} (${(listOf(entity.idColumn) + columns).joinToString(", ")}) " +
                "VALUES ($placeholders)"
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, key)
                fields.forEachIndexed { index, field ->
                    bindValue(statement, index + 2, field, storedValues[field.column].orEmpty())
                }
                if (hasImage) {
                    if (imageMediaId == null) {
                        statement.setNull(fields.size + 2, java.sql.Types.BIGINT)
                    } else {
                        statement.setLong(fields.size + 2, imageMediaId)
                    }
                }
                statement.executeUpdate()
            }
            return key
        }

        val assignments = columns.joinToString(", ") { "$it = ?" }
        val sql = "UPDATE ${entity.table} SET $assignments WHERE ${entity.idColumn} = ?"
        connection.prepareStatement(sql).use { statement ->
            fields.forEachIndexed { index, field ->
                bindValue(statement, index + 1, field, storedValues[field.column].orEmpty())
            }
            var next = fields.size + 1
            if (hasImage) {
                if (imageMediaId == null) {
                    statement.setNull(next, java.sql.Types.BIGINT)
                } else {
                    statement.setLong(next, imageMediaId)
                }
                next++
            }
            statement.setString(next, id)
            statement.executeUpdate()
        }
        return id
    }

    private fun saveUser(
        connection: Connection,
        id: String?,
        values: Map<String, String>,
    ): String {
        val name = values["name"].orEmpty().trim()
        val email = values["email"].orEmpty().trim().lowercase()
        val password = values["password"].orEmpty()
        if (id == null) {
            if (password.isBlank()) error("Kata sandi wajib diisi untuk pengguna baru.")
            if (email.isBlank()) error("Email wajib diisi.")
            val username = uniqueUsername(connection, email)
            val salt = PasswordHasher.randomSalt()
            val sql = """INSERT INTO users
                (name, username, email, phone, city, bio, avatar_uri, role, password_hash, password_salt)
                VALUES (?, ?, ?, ?, ?, ?, '', ?, ?, ?)"""
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, name)
                statement.setString(2, username)
                statement.setString(3, email)
                statement.setString(4, values["phone"].orEmpty().trim())
                statement.setString(5, values["city"].orEmpty().trim())
                statement.setString(6, values["bio"].orEmpty().trim())
                statement.setString(7, values["role"].orEmpty().ifBlank { "user" })
                statement.setString(8, PasswordHasher.hash(password, salt))
                statement.setString(9, salt)
                statement.executeUpdate()
            }
            return connection.prepareStatement("SELECT LAST_INSERT_ID()").use { statement ->
                statement.executeQuery().use { result ->
                    result.next()
                    result.getString(1)
                }
            }
        }

        val targetId = id.toLongOrNull() ?: error("ID pengguna tidak valid.")
        val newRole = values["role"].orEmpty().ifBlank { "user" }
        val currentRole = connection.prepareStatement(
            "SELECT role FROM users WHERE id = ?",
        ).use {
            it.setLong(1, targetId)
            it.executeQuery().use { result ->
                if (result.next()) result.getString("role") else null
            }
        } ?: error("Pengguna tidak ditemukan.")
        if (currentRole == "admin" && newRole != "admin") {
            val adminCount = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role = 'admin'",
            ).use { statement ->
                statement.executeQuery().use { result ->
                    if (result.next()) result.getInt(1) else 0
                }
            }
            if (adminCount <= 1) error("Minimal satu akun admin harus tersisa.")
        }
        val sql = if (password.isBlank()) {
            """UPDATE users SET name = ?, email = ?, phone = ?, city = ?, bio = ?, role = ?
               WHERE id = ?"""
        } else {
            """UPDATE users SET name = ?, email = ?, phone = ?, city = ?, bio = ?, role = ?,
               password_hash = ?, password_salt = ? WHERE id = ?"""
        }
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, name)
            statement.setString(2, email)
            statement.setString(3, values["phone"].orEmpty().trim())
            statement.setString(4, values["city"].orEmpty().trim())
            statement.setString(5, values["bio"].orEmpty().trim())
            statement.setString(6, newRole)
            if (password.isNotBlank()) {
                val salt = PasswordHasher.randomSalt()
                statement.setString(7, PasswordHasher.hash(password, salt))
                statement.setString(8, salt)
                statement.setLong(9, targetId)
            } else {
                statement.setLong(7, targetId)
            }
            statement.executeUpdate()
        }
        return id
    }

    private fun bindValue(
        statement: java.sql.PreparedStatement,
        index: Int,
        field: AdminField,
        raw: String,
    ) {
        when (field.type) {
            AdminFieldType.DECIMAL -> {
                val value = raw.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                statement.setDouble(index, value.coerceIn(0.0, 9.9))
            }
            AdminFieldType.INTEGER -> {
                statement.setInt(index, raw.trim().toIntOrNull() ?: 0)
            }
            AdminFieldType.LIST -> {
                val items = raw.split(',', '\n').map(String::trim).filter(String::isNotEmpty)
                statement.setString(index, TravelRepository.encodeList(items))
            }
            AdminFieldType.DESTINATION -> {
                if (raw.isBlank()) {
                    statement.setNull(index, java.sql.Types.VARCHAR)
                } else {
                    statement.setString(index, raw.trim())
                }
            }
            else -> statement.setString(index, raw.trim())
        }
    }

    private fun uniqueSlug(
        connection: Connection,
        entity: AdminEntity,
        title: String,
    ): String {
        val base = title.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "item" }
            .take(48)
        var candidate = base
        var suffix = 2
        while (connection.prepareStatement(
                "SELECT 1 FROM ${entity.table} WHERE ${entity.idColumn} = ? LIMIT 1",
            ).use {
                it.setString(1, candidate)
                it.executeQuery().use { result -> result.next() }
            }
        ) {
            candidate = "$base-$suffix"
            suffix++
        }
        return candidate
    }

    private fun uniqueUsername(connection: Connection, email: String): String {
        val base = email.substringBefore('@')
            .lowercase()
            .replace(Regex("[^a-z0-9._-]"), ".")
            .trim('.', '_', '-')
            .ifBlank { "user" }
            .take(45)
        var candidate = base
        var suffix = 2
        while (connection.prepareStatement("SELECT 1 FROM users WHERE username = ? LIMIT 1")
                .use {
                    it.setString(1, candidate)
                    it.executeQuery().use { result -> result.next() }
                }
        ) {
            candidate = "${base.take(40)}$suffix"
            suffix++
        }
        return candidate
    }
}
