package com.example.ujicobaaplikasi

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        val tombolMasuk = findViewById<MaterialButton>(R.id.tombolMasuk)

        // Tombol Masuk menjalankan proses login
        tombolMasuk.setOnClickListener {
            prosesLogin()
        }

        // Tombol "Selesai" pada keyboard juga bisa dipakai untuk login
        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                prosesLogin()
                true
            } else {
                false
            }
        }
    }

    private fun prosesLogin() {
        val username = etUsername.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()

        when {
            username.isEmpty() || password.isEmpty() -> {
                Toast.makeText(this, R.string.login_kosong, Toast.LENGTH_LONG).show()
            }

            username == getString(R.string.akun_admin_username) &&
                password == getString(R.string.akun_admin_password) -> {
                Toast.makeText(this, R.string.login_berhasil, Toast.LENGTH_SHORT).show()

                // Login berhasil: pindah ke halaman dashboard
                startActivity(Intent(this, HalamanDashboardActivity::class.java))
            }

            else -> {
                Toast.makeText(this, R.string.login_gagal, Toast.LENGTH_LONG).show()
            }
        }
    }
}
