package com.example.ujicobaaplikasi.ui.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.data.WrongPasswordException
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyImeInset
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.navigation.BottomNavView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordActivity : BaseActivity() {

    private lateinit var inputSaatIni: TextInputLayout
    private lateinit var inputBaru: TextInputLayout
    private lateinit var inputUlangi: TextInputLayout
    private lateinit var etSaatIni: TextInputEditText
    private lateinit var etBaru: TextInputEditText
    private lateinit var etUlangi: TextInputEditText
    private lateinit var bars: List<View>
    private lateinit var strengthLabel: TextView
    private lateinit var iconPanjang: ImageView
    private lateinit var iconHuruf: ImageView
    private lateinit var iconAngka: ImageView
    private lateinit var textPanjang: TextView
    private lateinit var textHuruf: TextView
    private lateinit var textAngka: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        findViewById<View>(R.id.changePasswordHeader).applyStatusBarInset()
        findViewById<NestedScrollView>(R.id.changePasswordScroll).applyImeInset()

        inputSaatIni = findViewById(R.id.inputPasswordSaatIni)
        inputBaru = findViewById(R.id.inputPasswordBaru)
        inputUlangi = findViewById(R.id.inputUlangiPassword)
        etSaatIni = findViewById(R.id.etPasswordSaatIni)
        etBaru = findViewById(R.id.etPasswordBaru)
        etUlangi = findViewById(R.id.etUlangiPassword)
        bars = listOf(
            findViewById(R.id.strengthBar1),
            findViewById(R.id.strengthBar2),
            findViewById(R.id.strengthBar3),
        )
        strengthLabel = findViewById(R.id.textKekuatan)
        iconPanjang = findViewById(R.id.iconAturanPanjang)
        iconHuruf = findViewById(R.id.iconAturanHuruf)
        iconAngka = findViewById(R.id.iconAturanAngka)
        textPanjang = findViewById(R.id.textAturanPanjang)
        textHuruf = findViewById(R.id.textAturanHuruf)
        textAngka = findViewById(R.id.textAturanAngka)

        findViewById<View>(R.id.tombolKembaliPassword).setOnClickListener { finish() }
        findViewById<View>(R.id.tombolPerbaruiPassword).setOnClickListener { submit() }

        etBaru.doAfterTextChanged { password ->
            updateStrength(password?.toString().orEmpty())
            inputBaru.error = null
        }
        etSaatIni.doAfterTextChanged { inputSaatIni.error = null }
        etUlangi.doAfterTextChanged { inputUlangi.error = null }
        etUlangi.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        updateStrength("")

        val nav = findViewById<BottomNavView>(R.id.changePasswordBottomNav)
        nav.selectedDestination = BottomNavDestination.PROFILE
        nav.setOnDestinationSelectedListener { destination ->
            MainActivity.open(this, destination)
            finish()
        }
    }

    private fun submit() {
        val current = etSaatIni.text?.toString().orEmpty()
        val newPassword = etBaru.text?.toString().orEmpty()
        val confirm = etUlangi.text?.toString().orEmpty()

        inputSaatIni.error = null
        inputBaru.error = null
        inputUlangi.error = null

        if (current.isBlank()) {
            inputSaatIni.error = getString(R.string.error_current_password)
            etSaatIni.requestFocus()
            return
        }
        if (newPassword.length < 8) {
            inputBaru.error = getString(R.string.error_password_short)
            etBaru.requestFocus()
            return
        }
        if (!hasRequiredCharacters(newPassword)) {
            inputBaru.error = getString(R.string.error_password_requirements)
            etBaru.requestFocus()
            return
        }
        if (newPassword == current) {
            inputBaru.error = getString(R.string.error_new_password_same)
            etBaru.requestFocus()
            return
        }
        if (newPassword != confirm) {
            inputUlangi.error = getString(R.string.error_password_mismatch)
            etUlangi.requestFocus()
            return
        }

        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(etUlangi.windowToken, 0)

        val submitButton = findViewById<View>(R.id.tombolPerbaruiPassword)
        submitButton.isEnabled = false
        AuthStore.changePassword(this, current, newPassword) { result ->
            submitButton.isEnabled = true
            result.fold(
                onSuccess = {
                    AppNotice.deferSuccess(this, R.string.password_changed)
                    finish()
                },
                onFailure = { throwable ->
                    if (throwable is WrongPasswordException) {
                        inputSaatIni.error = getString(R.string.error_current_password)
                        etSaatIni.requestFocus()
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

    private fun updateStrength(password: String) {
        val lengthOk = password.length >= 8
        val caseOk = password.any(Char::isUpperCase) && password.any(Char::isLowerCase)
        val numberOk = password.any(Char::isDigit)
        val score = listOf(lengthOk, caseOk, numberOk).count { it }

        updateRule(iconPanjang, textPanjang, lengthOk)
        updateRule(iconHuruf, textHuruf, caseOk)
        updateRule(iconAngka, textAngka, numberOk)

        val activeColor = when {
            score >= 3 -> R.color.hijau_lombok
            score == 2 -> R.color.coklat_tua
            score == 1 -> R.color.merah
            else -> R.color.garis_fokus
        }
        val active = ContextCompat.getColor(this, activeColor)
        bars.forEachIndexed { index, bar ->
            val color = if (index < score) active else ContextCompat.getColor(this, R.color.garis_lembut)
            bar.backgroundTintList = ColorStateList.valueOf(color)
        }

        strengthLabel.setText(
            when {
                score >= 3 -> R.string.password_strength_strong
                score == 2 -> R.string.password_strength_medium
                else -> R.string.password_strength_weak
            },
        )
        strengthLabel.setTextColor(active)
    }

    private fun updateRule(icon: ImageView, label: TextView, satisfied: Boolean) {
        val color = ContextCompat.getColor(
            this,
            if (satisfied) R.color.hijau_lombok else R.color.teks_tersier,
        )
        icon.imageTintList = ColorStateList.valueOf(color)
        label.setTextColor(color)
    }

    private fun hasRequiredCharacters(password: String): Boolean {
        return password.length >= 8 &&
            password.any(Char::isUpperCase) &&
            password.any(Char::isLowerCase) &&
            password.any(Char::isDigit)
    }
}
