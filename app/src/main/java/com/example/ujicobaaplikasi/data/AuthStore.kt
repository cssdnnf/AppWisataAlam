package com.example.ujicobaaplikasi.data

import android.content.Context
import org.json.JSONObject
import java.sql.Connection
import java.sql.ResultSet

/**
 * Autentikasi dan sesi. Kata sandi disimpan di tabel `users` sebagai
 * SHA-256(salt:kata_sandi); sesi ringan disimpan di SharedPreferences agar
 * aplikasi bisa membuka dashboard tanpa menunggu jaringan.
 */
object AuthStore {

    private const val PREFS = "jelajah_auth"
    private const val KEY_ACCOUNT = "session_account"
    private const val KEY_LOGGED_IN = "logged_in"

    data class Account(
        val id: Long,
        val name: String,
        val username: String,
        val email: String,
        val phone: String,
        val city: String,
        val bio: String,
        val avatarUri: String,
        val role: String,
    ) {
        val isAdmin: Boolean get() = role == "admin"
    }

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LOGGED_IN, false) && currentAccount(context) != null

    fun currentAccount(context: Context): Account? {
        val raw = prefs(context).getString(KEY_ACCOUNT, null) ?: return null
        return runCatching { parseAccount(JSONObject(raw)) }.getOrNull()
    }

    fun currentUserId(context: Context): Long? = currentAccount(context)?.id

    fun login(
        context: Context,
        identifier: String,
        password: String,
        callback: (Result<Account>) -> Unit,
    ) {
        val normalized = identifier.trim().lowercase()
        Db.async({ connection ->
            val account = findByIdentifier(connection, normalized)
                ?: throw InvalidCredentialsException()
            if (!sameHash(password, account.salt, account.hash)) {
                throw InvalidCredentialsException()
            }
            connection.prepareStatement("UPDATE users SET last_login_at = NOW() WHERE id = ?")
                .use { it.setLong(1, account.id); it.executeUpdate() }
            account.toAccount()
        }) { result ->
            result.onSuccess { saveSession(context, it) }
            callback(result)
        }
    }

    fun register(
        context: Context,
        name: String,
        email: String,
        phone: String,
        password: String,
        callback: (Result<Account>) -> Unit,
    ) {
        val normalizedEmail = email.trim().lowercase()
        Db.async({ connection ->
            val existing = connection.prepareStatement(
                "SELECT id FROM users WHERE email = ? LIMIT 1",
            ).use { statement ->
                statement.setString(1, normalizedEmail)
                statement.executeQuery().use { if (it.next()) it.getLong(1) else null }
            }
            if (existing != null) throw DuplicateEmailException()

            val username = uniqueUsername(connection, normalizedEmail)
            val salt = PasswordHasher.randomSalt()
            connection.prepareStatement(
                """INSERT INTO users
                   (name, username, email, phone, city, bio, avatar_uri, role, password_hash, password_salt)
                   VALUES (?, ?, ?, ?, '', '', '', 'user', ?, ?)""",
            ).use { statement ->
                statement.setString(1, name.trim())
                statement.setString(2, username)
                statement.setString(3, normalizedEmail)
                statement.setString(4, phone.trim())
                statement.setString(5, PasswordHasher.hash(password, salt))
                statement.setString(6, salt)
                statement.executeUpdate()
            }

            findByIdentifier(connection, normalizedEmail)?.toAccount()
                ?: throw IllegalStateException("Akun gagal dibuat.")
        }) { result ->
            result.onSuccess { saveSession(context, it) }
            callback(result)
        }
    }

    fun requestPasswordReset(
        email: String,
        callback: (Result<String>) -> Unit,
    ) {
        val normalizedEmail = email.trim().lowercase()
        Db.async({ connection ->
            val userId = connection.prepareStatement(
                "SELECT id FROM users WHERE email = ? LIMIT 1",
            ).use { statement ->
                statement.setString(1, normalizedEmail)
                statement.executeQuery().use { if (it.next()) it.getLong(1) else null }
            } ?: throw EmailNotFoundException()

            val code = PasswordHasher.resetCode()
            connection.prepareStatement(
                "DELETE FROM password_resets WHERE user_id = ? AND used_at IS NULL",
            ).use { it.setLong(1, userId); it.executeUpdate() }
            connection.prepareStatement(
                """INSERT INTO password_resets (user_id, code_hash, expires_at)
                   VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 15 MINUTE))""",
            ).use { statement ->
                statement.setLong(1, userId)
                statement.setString(2, PasswordHasher.resetCodeHash(code))
                statement.executeUpdate()
            }
            code
        }, callback)
    }

    fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        callback: (Result<Unit>) -> Unit,
    ) {
        val normalizedEmail = email.trim().lowercase()
        Db.async({ connection ->
            connection.autoCommit = false
            try {
                val userId = connection.prepareStatement(
                    "SELECT id FROM users WHERE email = ? LIMIT 1",
                ).use { statement ->
                    statement.setString(1, normalizedEmail)
                    statement.executeQuery().use { if (it.next()) it.getLong(1) else null }
                } ?: throw EmailNotFoundException()

                val resetId = connection.prepareStatement(
                    """SELECT id, code_hash FROM password_resets
                       WHERE user_id = ? AND used_at IS NULL AND expires_at > NOW()
                       ORDER BY id DESC LIMIT 1""",
                ).use { statement ->
                    statement.setLong(1, userId)
                    statement.executeQuery().use { result ->
                        if (!result.next()) throw InvalidResetCodeException()
                        if (result.getString("code_hash") !=
                            PasswordHasher.resetCodeHash(code.trim())
                        ) {
                            throw InvalidResetCodeException()
                        }
                        result.getLong("id")
                    }
                }

                val salt = PasswordHasher.randomSalt()
                connection.prepareStatement(
                    "UPDATE users SET password_hash = ?, password_salt = ? WHERE id = ?",
                ).use { statement ->
                    statement.setString(1, PasswordHasher.hash(newPassword, salt))
                    statement.setString(2, salt)
                    statement.setLong(3, userId)
                    statement.executeUpdate()
                }
                connection.prepareStatement(
                    "UPDATE password_resets SET used_at = NOW() WHERE id = ?",
                ).use { it.setLong(1, resetId); it.executeUpdate() }
                connection.commit()
            } catch (throwable: Throwable) {
                runCatching { connection.rollback() }
                throw throwable
            } finally {
                connection.autoCommit = true
            }
        }, callback)
    }

    fun validateSession(context: Context, callback: (Result<Account>) -> Unit) {
        val account = currentAccount(context)
        if (account == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        Db.async({ connection ->
            val row = findById(connection, account.id)
                ?: throw InvalidCredentialsException()
            row.toAccount()
        }) { result ->
            result.onSuccess { saveSession(context, it) }
            callback(result)
        }
    }

    fun updateProfile(
        context: Context,
        name: String,
        email: String,
        phone: String,
        city: String,
        bio: String,
        avatarUri: String,
        callback: (Result<Account>) -> Unit,
    ) {
        val current = currentAccount(context)
        if (current == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        val normalizedEmail = email.trim().lowercase()
        Db.async({ connection ->
            if (normalizedEmail != current.email) {
                val taken = connection.prepareStatement(
                    "SELECT id FROM users WHERE email = ? AND id <> ? LIMIT 1",
                ).use { statement ->
                    statement.setString(1, normalizedEmail)
                    statement.setLong(2, current.id)
                    statement.executeQuery().use { it.next() }
                }
                if (taken) throw DuplicateEmailException()
            }
            connection.prepareStatement(
                """UPDATE users SET name = ?, email = ?, phone = ?, city = ?, bio = ?, avatar_uri = ?
                   WHERE id = ?""",
            ).use { statement ->
                statement.setString(1, name.trim())
                statement.setString(2, normalizedEmail)
                statement.setString(3, phone.trim())
                statement.setString(4, city.trim())
                statement.setString(5, bio.trim())
                statement.setString(6, avatarUri)
                statement.setLong(7, current.id)
                statement.executeUpdate()
            }
            findById(connection, current.id)?.toAccount()
                ?: throw IllegalStateException("Akun tidak ditemukan.")
        }) { result ->
            result.onSuccess { saveSession(context, it) }
            callback(result)
        }
    }

    fun changePassword(
        context: Context,
        currentPassword: String,
        newPassword: String,
        callback: (Result<Unit>) -> Unit,
    ) {
        val current = currentAccount(context)
        if (current == null) {
            callback(Result.failure(InvalidCredentialsException()))
            return
        }
        Db.async({ connection ->
            val (hash, salt) = connection.prepareStatement(
                "SELECT password_hash, password_salt FROM users WHERE id = ?",
            ).use { statement ->
                statement.setLong(1, current.id)
                statement.executeQuery().use { result ->
                    if (!result.next()) throw InvalidCredentialsException()
                    result.getString("password_hash") to result.getString("password_salt")
                }
            }
            if (!sameHash(currentPassword, salt, hash)) throw WrongPasswordException()

            val newSalt = PasswordHasher.randomSalt()
            connection.prepareStatement(
                "UPDATE users SET password_hash = ?, password_salt = ? WHERE id = ?",
            ).use { statement ->
                statement.setString(1, PasswordHasher.hash(newPassword, newSalt))
                statement.setString(2, newSalt)
                statement.setLong(3, current.id)
                statement.executeUpdate()
            }
        }, callback)
    }

    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
        AppData.clear()
        TravelRepository.clear()
    }

    private data class UserRow(
        val id: Long,
        val name: String,
        val username: String,
        val email: String,
        val phone: String,
        val city: String,
        val bio: String,
        val avatarUri: String,
        val role: String,
        val hash: String,
        val salt: String,
    ) {
        fun toAccount() = Account(
            id = id,
            name = name,
            username = username,
            email = email,
            phone = phone,
            city = city,
            bio = bio,
            avatarUri = avatarUri,
            role = role,
        )
    }

    private fun findByIdentifier(connection: Connection, identifier: String): UserRow? {
        return connection.prepareStatement(
            "SELECT * FROM users WHERE email = ? OR username = ? LIMIT 1",
        ).use { statement ->
            statement.setString(1, identifier)
            statement.setString(2, identifier)
            statement.executeQuery().use { result -> if (result.next()) mapRow(result) else null }
        }
    }

    private fun findById(connection: Connection, id: Long): UserRow? {
        return connection.prepareStatement("SELECT * FROM users WHERE id = ? LIMIT 1")
            .use { statement ->
                statement.setLong(1, id)
                statement.executeQuery().use { result -> if (result.next()) mapRow(result) else null }
            }
    }

    private fun mapRow(result: ResultSet) = UserRow(
        id = result.getLong("id"),
        name = result.getString("name").orEmpty(),
        username = result.getString("username").orEmpty(),
        email = result.getString("email").orEmpty(),
        phone = result.getString("phone").orEmpty(),
        city = result.getString("city").orEmpty(),
        bio = result.getString("bio").orEmpty(),
        avatarUri = result.getString("avatar_uri").orEmpty(),
        role = result.getString("role") ?: "user",
        hash = result.getString("password_hash").orEmpty(),
        salt = result.getString("password_salt").orEmpty(),
    )

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
                .use { statement ->
                    statement.setString(1, candidate)
                    statement.executeQuery().use { it.next() }
                }
        ) {
            candidate = "${base.take(40)}$suffix"
            suffix++
        }
        return candidate
    }

    private fun sameHash(password: String, salt: String, expected: String): Boolean {
        val actual = PasswordHasher.hash(password, salt)
        if (actual.length != expected.length) return false
        var result = 0
        for (index in actual.indices) {
            result = result or (actual[index].code xor expected[index].code)
        }
        return result == 0
    }

    private fun saveSession(context: Context, account: Account) {
        val json = JSONObject().apply {
            put("id", account.id)
            put("name", account.name)
            put("username", account.username)
            put("email", account.email)
            put("phone", account.phone)
            put("city", account.city)
            put("bio", account.bio)
            put("avatar", account.avatarUri)
            put("role", account.role)
        }
        prefs(context).edit()
            .putString(KEY_ACCOUNT, json.toString())
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    private fun parseAccount(json: JSONObject) = Account(
        id = json.optLong("id"),
        name = json.optString("name"),
        username = json.optString("username"),
        email = json.optString("email"),
        phone = json.optString("phone"),
        city = json.optString("city"),
        bio = json.optString("bio"),
        avatarUri = json.optString("avatar"),
        role = json.optString("role", "user"),
    )

    private fun prefs(context: Context) = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
