package com.example.ujicobaaplikasi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.data.InvalidCredentialsException
import com.example.ujicobaaplikasi.ui.admin.AdminActivity
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyImeInset
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : BaseActivity() {

    private lateinit var inputIdentifier: TextInputLayout
    private lateinit var inputPassword: TextInputLayout
    private lateinit var etIdentifier: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private lateinit var forgotButton: TextView
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        useLightSystemIcons()

        val content = findViewById<View>(R.id.loginContent)
        val scroll = findViewById<NestedScrollView>(R.id.loginScroll)
        content.applyStatusBarInset()
        scroll.applyImeInset()

        inputIdentifier = findViewById(R.id.inputIdentifier)
        inputPassword = findViewById(R.id.inputPassword)
        etIdentifier = findViewById(R.id.etIdentifier)
        etPassword = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.tombolMasuk)
        registerButton = findViewById(R.id.tombolDaftar)
        forgotButton = findViewById(R.id.tombolLupaPassword)

        loginButton.setOnClickListener { attemptLogin() }
        registerButton.setOnClickListener {
            if (!busy) startActivity(Intent(this, RegisterActivity::class.java))
        }
        forgotButton.setOnClickListener {
            if (!busy) startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else {
                false
            }
        }

        etIdentifier.doAfterTextChanged { inputIdentifier.error = null }
        etPassword.doAfterTextChanged { inputPassword.error = null }

        if (AuthStore.isLoggedIn(this)) {
            restoreSession()
        }
    }

    private fun restoreSession() {
        setBusy(true)
        AuthStore.validateSession(this) { result ->
            result.fold(
                onSuccess = { openDashboard(it) },
                onFailure = { throwable ->
                    if (throwable is InvalidCredentialsException) {
                        AuthStore.logout(this)
                    } else {
                        AppNotice.error(
                            this,
                            throwable.message ?: getString(R.string.error_network),
                        )
                    }
                    setBusy(false)
                },
            )
        }
    }

    private fun attemptLogin() {
        if (busy) return
        val identifier = etIdentifier.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()

        inputIdentifier.error = null
        inputPassword.error = null

        if (identifier.isBlank()) {
            inputIdentifier.error = getString(R.string.error_login_empty)
            etIdentifier.requestFocus()
            return
        }
        if (password.isBlank()) {
            inputPassword.error = getString(R.string.error_login_empty)
            etPassword.requestFocus()
            return
        }

        hideKeyboard()
        setBusy(true)
        AuthStore.login(this, identifier, password) { result ->
            result.fold(
                onSuccess = { account -> openDashboard(account) },
                onFailure = {
                    setBusy(false)
                    inputPassword.error = getString(R.string.error_login_invalid)
                    etPassword.requestFocus()
                },
            )
        }
    }

    private fun openDashboard(account: AuthStore.Account) {
        val target = if (account.isAdmin) {
            AdminActivity::class.java
        } else {
            MainActivity::class.java
        }
        startActivity(
            Intent(this, target).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }

    private fun setBusy(value: Boolean) {
        busy = value
        loginButton.isEnabled = !value
        registerButton.isEnabled = !value
        forgotButton.isEnabled = !value
        etIdentifier.isEnabled = !value
        etPassword.isEnabled = !value
        loginButton.text = getString(if (value) R.string.action_processing else R.string.action_login)
    }

    private fun hideKeyboard() {
        val manager = getSystemService(InputMethodManager::class.java)
        manager?.hideSoftInputFromWindow(etPassword.windowToken, 0)
    }
}
