package com.example.ujicobaaplikasi.data

import android.content.Context

data class MediaRecord(
    val id: Long,
    val name: String,
    val thumbnail: ByteArray,
    val width: Int,
    val height: Int,
)

object MediaRepository {

    fun list(callback: (Result<List<MediaRecord>>) -> Unit) {
        Db.async({ connection ->
            connection.prepareStatement(
                "SELECT id, name, thumbnail, width, height FROM media_library ORDER BY created_at DESC, id DESC",
            ).use { statement ->
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                MediaRecord(
                                    id = result.getLong("id"),
                                    name = result.getString("name").orEmpty(),
                                    thumbnail = result.getBytes("thumbnail") ?: ByteArray(0),
                                    width = result.getInt("width"),
                                    height = result.getInt("height"),
                                ),
                            )
                        }
                    }
                }
            }
        }, callback)
    }

    fun upload(
        context: Context,
        image: PreparedImage,
        callback: (Result<MediaRecord>) -> Unit,
    ) {
        val userId = AuthStore.currentUserId(context)
        Db.async({ connection ->
            connection.prepareStatement(
                """INSERT INTO media_library
                   (name, mime, size_bytes, width, height, thumbnail, file_data, created_by)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
            ).use { statement ->
                statement.setString(1, image.name)
                statement.setString(2, image.mime)
                statement.setInt(3, image.bytes.size)
                statement.setInt(4, image.width)
                statement.setInt(5, image.height)
                statement.setBytes(6, image.thumbnail)
                statement.setBytes(7, image.bytes)
                if (userId == null) statement.setNull(8, java.sql.Types.BIGINT)
                else statement.setLong(8, userId)
                statement.executeUpdate()
            }
            val id = connection.prepareStatement("SELECT LAST_INSERT_ID()").use { statement ->
                statement.executeQuery().use { result ->
                    result.next()
                    result.getLong(1)
                }
            }
            MediaRecord(id, image.name, image.thumbnail, image.width, image.height)
        }, callback)
    }

    fun loadMeta(id: Long, callback: (Result<MediaRecord?>) -> Unit) {
        Db.async({ connection ->
            connection.prepareStatement(
                "SELECT id, name, thumbnail, width, height FROM media_library WHERE id = ?",
            ).use { statement ->
                statement.setLong(1, id)
                statement.executeQuery().use { result ->
                    if (!result.next()) null else MediaRecord(
                        id = result.getLong("id"),
                        name = result.getString("name").orEmpty(),
                        thumbnail = result.getBytes("thumbnail") ?: ByteArray(0),
                        width = result.getInt("width"),
                        height = result.getInt("height"),
                    )
                }
            }
        }, callback)
    }

    fun keyFor(id: Long): String = "media:$id"

    fun parseMediaId(value: String?): Long? {
        if (value == null || !value.startsWith("media:")) return null
        return value.substringAfter("media:").toLongOrNull()
    }

    fun parseBuiltInKey(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return if (value.startsWith("media:")) null else value.removePrefix("builtin:")
    }
}
