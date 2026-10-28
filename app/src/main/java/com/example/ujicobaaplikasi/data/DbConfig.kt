package com.example.ujicobaaplikasi.data

/**
 * Konfigurasi koneksi database demo.
 *
 * Aplikasi terhubung langsung ke MariaDB/MySQL untuk keperluan prototipe.
 * Untuk produksi, kredensial ini harus dipindahkan ke backend API dan tidak
 * boleh ikut didistribusikan di dalam APK.
 */
object DbConfig {
    const val HOST = "127.0.0.1"
    const val PORT = 3306
    const val DATABASE = "lombokapps"
    const val USER = "apps"
    const val PASSWORD = ""
}
