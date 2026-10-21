package com.example.ujicobaaplikasi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class HalamanDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halaman_dashboard)

        val tombolBacaSelengkapnya = findViewById<MaterialButton>(R.id.tombolBacaSelengkapnya)
        val tombolLogout = findViewById<MaterialButton>(R.id.tombolLogout)

        // Membuka halaman informasi lengkap tentang alam Lombok
        tombolBacaSelengkapnya.setOnClickListener {
            startActivity(Intent(this, HalamanInformasiActivity::class.java))
        }

        // Logout: tampilkan notifikasi lalu kembali ke halaman login yang baru
        tombolLogout.setOnClickListener {
            Toast.makeText(this, R.string.logout_berhasil, Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
