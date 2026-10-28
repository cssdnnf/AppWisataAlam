package com.example.ujicobaaplikasi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.data.DuplicateEmailException
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyImeInset
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : BaseActivity() {

    private lateinit var inputNama: TextInputLayout
    private lateinit var inputEmail: TextInputLayout
    private lateinit var inputTelepon: TextInputLayout
    private lateinit var inputPassword: TextInputLayout
    private lateinit var inputKonfirmasi: TextInputLayout
    private lateinit var etNama: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etTelepon: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etKonfirmasi: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViewById<LinearLayout>(R.id.headerDaftar).applyStatusBarInset()
        findViewById<NestedScrollView>(R.id.registerScroll).applyImeInset()

        inputNama = findViewById(R.id.inputNama)
        inputEmail = findViewById(R.id.inputEmailDaftar)
        inputTelepon = findViewById(R.id.inputTelepon)
        inputPassword = findViewById(R.id.inputPasswordDaftar)
        inputKonfirmasi = findViewById(R.id.inputKonfirmasi)
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmailDaftar)
        etTelepon = findViewById(R.id.etTelepon)
        etPassword = findViewById(R.id.etPasswordDaftar)
        etKonfirmasi = findViewById(R.id.etKonfirmasi)
        submitButton = findViewById(R.id.tombolBuatAkun)

        findViewById<MaterialButton>(R.id.tombolKembaliDaftar).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.tombolMasukDariDaftar).setOnClickListener { finish() }
        submitButton.setOnClickListener { attemptRegister() }

        etKonfirmasi.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister()
                true
            } else {
                false
            }
        }

        listOf(
            etNama to inputNama,
            etEmail to inputEmail,
            etTelepon to inputTelepon,
            etPassword to inputPassword,
            etKonfirmasi to inputKonfirmasi,
        ).forEach { (field, layout) ->
            field.doAfterTextChanged { layout.error = null }
        }
    }

    private fun attemptRegister() {
        if (busy) return
        val nama = etNama.text?.toString()?.trim().orEmpty()
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val telepon = etTelepon.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        val konfirmasi = etKonfirmasi.text?.toString().orEmpty()

        listOf(inputNama, inputEmail, inputTelepon, inputPassword, inputKonfirmasi)
            .forEach { it.error = null }

        var firstInvalid: TextInputEditText? = null

        if (nama.isBlank()) {
            inputNama.error = getString(R.string.error_name_required)
            firstInvalid = etNama
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.error = getString(R.string.error_email_invalid)
            firstInvalid = firstInvalid ?: etEmail
        }
        if (telepon.count(Char::isDigit) < 9) {
            inputTelepon.error = getString(R.string.error_phone_invalid)
            firstInvalid = firstInvalid ?: etTelepon
        }
        if (password.length < 8) {
            inputPassword.error = getString(R.string.error_password_short)
            firstInvalid = firstInvalid ?: etPassword
        } else if (!password.any(Char::isUpperCase) ||
            !password.any(Char::isLowerCase) ||
            !password.any(Char::isDigit)
        ) {
            inputPassword.error = getString(R.string.error_password_requirements)
            firstInvalid = firstInvalid ?: etPassword
        }
        if (password != konfirmasi) {
            inputKonfirmasi.error = getString(R.string.error_password_mismatch)
            firstInvalid = firstInvalid ?: etKonfirmasi
        }

        firstInvalid?.let {
            it.requestFocus()
            return
        }

        hideKeyboard()
        setBusy(true)
        AuthStore.register(this, nama, email, telepon, password) { result ->
            result.fold(
                onSuccess = {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        },
                    )
                    finish()
                },
                onFailure = { throwable ->
                    setBusy(false)
                    if (throwable is DuplicateEmailException) {
                        inputEmail.error = getString(R.string.error_email_used)
                        etEmail.requestFocus()
                    } else {
                        AppNotice.error(
                            this,
                            throwable.message ?: getString(R.string.error_network),
                        )
                    }
                },
            )
        }
    }

    private fun setBusy(value: Boolean) {
        busy = value
        submitButton.isEnabled = !value
        submitButton.text = getString(
            if (value) R.string.action_processing else R.string.action_create_account,
        )
    }

    private fun hideKeyboard() {
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(etKonfirmasi.windowToken, 0)
    }
}
