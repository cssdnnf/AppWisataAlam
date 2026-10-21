package com.example.ujicobaaplikasi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class HalamanInformasiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halaman_informasi)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarInformasi)
        val tombolKembali = findViewById<MaterialButton>(R.id.tombolKembaliInformasi)

        // Tombol panah pada toolbar kembali ke dashboard
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Tombol di bagian bawah halaman juga kembali ke dashboard
        tombolKembali.setOnClickListener {
            finish()
        }
    }
}
