package com.example.ujicobaaplikasi

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HalamanKeduaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hanya cukup memanggil layout XML-nya saja
        setContentView(R.layout.activity_halaman_kedua)

        // 1. Kenalkan tombol dari XML ke Kotlin
        val tombolKembali = findViewById<Button>(R.id.tombolKembali)

        // 2. Beri perintah saat diklik
        tombolKembali.setOnClickListener {
            // Menutup Halaman Kedua, otomatis kembali ke Halaman Pertama
            finish()

        }
    }
}