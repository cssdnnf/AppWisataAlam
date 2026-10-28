package com.example.ujicobaaplikasi.data

import android.os.Handler
import android.os.Looper
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors

/**
 * Akses JDBC ke database. Semua operasi jaringan wajib lewat [async] agar
 * tidak menyentuh main thread.
 */
object Db {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newFixedThreadPool(4)
    private val pool = ArrayBlockingQueue<Connection>(4)

    private val url = buildString {
        append("jdbc:mysql://")
        append(DbConfig.HOST)
        append(':')
        append(DbConfig.PORT)
        append('/')
        append(DbConfig.DATABASE)
        append("?useUnicode=true&characterEncoding=UTF-8&useSSL=false")
        append("&serverTimezone=UTC&connectTimeout=15000&socketTimeout=30000")
        append("&autoReconnect=true&failOverReadOnly=false")
    }

    init {
        // Android's DriverManager tidak selalu membaca META-INF/services.
        Class.forName("com.mysql.jdbc.Driver")
    }

    fun <T> async(block: (Connection) -> T, onResult: (Result<T>) -> Unit) {
        executor.execute {
            val result = runCatching { withConnection(block) }
            mainHandler.post { onResult(result) }
        }
    }

    fun <T> withConnection(block: (Connection) -> T): T {
        val connection = acquire()
        return try {
            block(connection)
        } finally {
            release(connection)
        }
    }

    private fun acquire(): Connection {
        while (true) {
            val connection = pool.poll() ?: return openConnection()
            val valid = runCatching { connection.isValid(3) }.getOrDefault(false)
            if (valid) return connection
            runCatching { connection.close() }
        }
    }

    private fun release(connection: Connection) {
        if (!pool.offer(connection)) {
            runCatching { connection.close() }
        }
    }

    private fun openConnection(): Connection {
        return DriverManager.getConnection(url, DbConfig.USER, DbConfig.PASSWORD).apply {
            autoCommit = true
        }
    }
}

class DuplicateEmailException : Exception("Email sudah terdaftar.")
class DuplicateUsernameException : Exception("Username sudah dipakai.")
class InvalidCredentialsException : Exception("Email atau kata sandi salah.")
class EmailNotFoundException : Exception("Email tidak terdaftar.")
class InvalidResetCodeException : Exception("Kode reset tidak valid atau kedaluwarsa.")
class WrongPasswordException : Exception("Kata sandi saat ini salah.")
