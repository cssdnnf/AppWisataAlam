package com.example.ujicobaaplikasi.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.data.EmailNotFoundException
import com.example.ujicobaaplikasi.data.InvalidResetCodeException
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyImeInset
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var inputEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var formSection: LinearLayout
    private lateinit var success: LinearLayout
    private lateinit var inputCode: TextInputLayout
    private lateinit var etCode: TextInputEditText
    private lateinit var inputPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var inputConfirm: TextInputLayout
    private lateinit var etConfirm: TextInputEditText
    private lateinit var sendButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var resendButton: MaterialButton
    private lateinit var codeView: TextView
    private var submittedEmail: String = ""
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        findViewById<LinearLayout>(R.id.headerLupa).applyStatusBarInset()
        findViewById<NestedScrollView>(R.id.forgotScroll).applyImeInset()

        inputEmail = findViewById(R.id.inputEmailLupa)
        etEmail = findViewById(R.id.etEmailLupa)
        formSection = findViewById(R.id.bagianFormLupa)
        success = findViewById(R.id.layoutSukses)
        inputCode = findViewById(R.id.inputKodeReset)
        etCode = findViewById(R.id.etKodeReset)
        inputPassword = findViewById(R.id.inputPasswordReset)
        etPassword = findViewById(R.id.etPasswordReset)
        inputConfirm = findViewById(R.id.inputKonfirmasiReset)
        etConfirm = findViewById(R.id.etKonfirmasiReset)
        sendButton = findViewById(R.id.tombolKirimReset)
        saveButton = findViewById(R.id.tombolSimpanReset)
        resendButton = findViewById(R.id.tombolKirimUlang)
        codeView = findViewById(R.id.teksKodeReset)

        findViewById<MaterialButton>(R.id.tombolKembaliLupa).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.tombolKembaliLogin).setOnClickListener { finish() }
        sendButton.setOnClickListener { requestCode(showSuccessView = true) }
        resendButton.setOnClickListener { requestCode(showSuccessView = false) }
        saveButton.setOnClickListener { saveNewPassword() }

        etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                requestCode(showSuccessView = true)
                true
            } else {
                false
            }
        }
        etConfirm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveNewPassword()
                true
            } else {
                false
            }
        }
        etEmail.doAfterTextChanged { inputEmail.error = null }
        etCode.doAfterTextChanged { inputCode.error = null }
        etPassword.doAfterTextChanged { inputPassword.error = null }
        etConfirm.doAfterTextChanged { inputConfirm.error = null }
    }

    private fun requestCode(showSuccessView: Boolean) {
        if (busy) return
        val email = etEmail.text?.toString()?.trim().orEmpty()
        inputEmail.error = null

        if (email.isBlank()) {
            inputEmail.error = getString(R.string.error_email_empty)
            etEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.error = getString(R.string.error_email_invalid)
            etEmail.requestFocus()
            return
        }

        hideKeyboard()
        setBusy(true, showSuccessView)
        AuthStore.requestPasswordReset(email) { result ->
            setBusy(false, showSuccessView)
            result.fold(
                onSuccess = { code ->
                    submittedEmail = email
                    showSuccess(code, showSuccessView)
                },
                onFailure = { throwable ->
                    if (throwable is EmailNotFoundException) {
                        inputEmail.error = getString(R.string.error_email_not_found)
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

    private fun showSuccess(code: String, switchView: Boolean) {
        codeView.text = code
        if (switchView) {
            findViewById<TextView>(R.id.teksEmailSukses).text =
                getString(R.string.forgot_success_sub, submittedEmail)
            formSection.visibility = View.GONE
            success.visibility = View.VISIBLE
            etCode.requestFocus()
        } else {
            AppNotice.success(this, R.string.forgot_resent)
        }
    }

    private fun saveNewPassword() {
        if (busy) return
        val code = etCode.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        val confirm = etConfirm.text?.toString().orEmpty()

        inputCode.error = null
        inputPassword.error = null
        inputConfirm.error = null

        if (code.length != 6) {
            inputCode.error = getString(R.string.error_reset_code)
            etCode.requestFocus()
            return
        }
        if (password.length < 8) {
            inputPassword.error = getString(R.string.error_password_short)
            etPassword.requestFocus()
            return
        }
        if (!password.any(Char::isUpperCase) ||
            !password.any(Char::isLowerCase) ||
            !password.any(Char::isDigit)
        ) {
            inputPassword.error = getString(R.string.error_password_requirements)
            etPassword.requestFocus()
            return
        }
        if (password != confirm) {
            inputConfirm.error = getString(R.string.error_password_mismatch)
            etConfirm.requestFocus()
            return
        }

        hideKeyboard()
        setBusy(true, true)
        AuthStore.resetPassword(submittedEmail, code, password) { result ->
            setBusy(false, true)
            result.fold(
                onSuccess = {
                    AppNotice.deferSuccess(this, R.string.password_reset_success)
                    finish()
                },
                onFailure = { throwable ->
                    if (throwable is InvalidResetCodeException) {
                        inputCode.error = getString(R.string.error_reset_code)
                        etCode.requestFocus()
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

    private fun setBusy(value: Boolean, successView: Boolean) {
        busy = value
        if (successView) {
            saveButton.isEnabled = !value
            resendButton.isEnabled = !value
            saveButton.text = getString(
                if (value) R.string.action_processing else R.string.action_save_new_password,
            )
        } else {
            sendButton.isEnabled = !value
            sendButton.text = getString(
                if (value) R.string.action_processing else R.string.action_send_reset,
            )
        }
    }

    private fun hideKeyboard() {
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(etEmail.windowToken, 0)
    }
}
