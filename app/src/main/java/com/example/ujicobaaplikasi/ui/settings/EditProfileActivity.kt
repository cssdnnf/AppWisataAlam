package com.example.ujicobaaplikasi.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.data.DuplicateEmailException
import com.example.ujicobaaplikasi.ui.common.AvatarView
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyImeInset
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.example.ujicobaaplikasi.ui.main.MainActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.navigation.BottomNavView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditProfileActivity : BaseActivity() {

    private lateinit var avatar: AvatarView
    private lateinit var inputNama: TextInputLayout
    private lateinit var inputEmail: TextInputLayout
    private lateinit var inputTelepon: TextInputLayout
    private lateinit var etNama: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etTelepon: TextInputEditText
    private lateinit var etKota: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var removePhotoButton: MaterialButton
    private var selectedAvatarUri: String = ""

    private val photoPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
            // Photo Picker grants temporary access; AvatarView falls back to initials.
        }
        selectedAvatarUri = uri.toString()
        avatar.setProfile(etNama.text?.toString(), selectedAvatarUri)
        updateRemovePhotoVisibility()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val account = AuthStore.currentAccount(this)
        if (account == null) {
            finish()
            return
        }

        findViewById<View>(R.id.editProfileHeader).applyStatusBarInset()
        findViewById<NestedScrollView>(R.id.editProfileScroll).applyImeInset()

        avatar = findViewById(R.id.editAvatar)
        inputNama = findViewById(R.id.inputEditNama)
        inputEmail = findViewById(R.id.inputEditEmail)
        inputTelepon = findViewById(R.id.inputEditTelepon)
        etNama = findViewById(R.id.etEditNama)
        etEmail = findViewById(R.id.etEditEmail)
        etTelepon = findViewById(R.id.etEditTelepon)
        etKota = findViewById(R.id.etEditKota)
        etBio = findViewById(R.id.etEditBio)
        removePhotoButton = findViewById(R.id.tombolHapusFoto)

        selectedAvatarUri = account.avatarUri
        etNama.setText(account.name)
        etEmail.setText(account.email)
        etTelepon.setText(account.phone)
        etKota.setText(account.city)
        etBio.setText(account.bio)
        avatar.setProfile(account.name, selectedAvatarUri)
        updateRemovePhotoVisibility()

        findViewById<View>(R.id.tombolKembaliEditProfil).setOnClickListener { finish() }
        findViewById<View>(R.id.avatarPicker).setOnClickListener { openPhotoPicker() }
        findViewById<View>(R.id.tombolGantiFoto).setOnClickListener { openPhotoPicker() }
        removePhotoButton.setOnClickListener {
            selectedAvatarUri = ""
            avatar.clearPhoto()
            updateRemovePhotoVisibility()
        }
        findViewById<View>(R.id.tombolSimpanProfil).setOnClickListener { saveProfile() }

        listOf(
            etNama to inputNama,
            etEmail to inputEmail,
            etTelepon to inputTelepon,
        ).forEach { (field, layout) ->
            field.doAfterTextChanged { layout.error = null }
        }

        val nav = findViewById<BottomNavView>(R.id.editProfileBottomNav)
        nav.selectedDestination = BottomNavDestination.PROFILE
        nav.setOnDestinationSelectedListener { destination ->
            MainActivity.open(this, destination)
            finish()
        }
    }

    private fun openPhotoPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    private fun updateRemovePhotoVisibility() {
        removePhotoButton.visibility =
            if (selectedAvatarUri.isBlank()) View.GONE else View.VISIBLE
    }

    private fun saveProfile() {
        val nama = etNama.text?.toString()?.trim().orEmpty()
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val telepon = etTelepon.text?.toString()?.trim().orEmpty()
        val kota = etKota.text?.toString()?.trim().orEmpty()
        val bio = etBio.text?.toString()?.trim().orEmpty()

        inputNama.error = null
        inputEmail.error = null
        inputTelepon.error = null

        var firstInvalid: View? = null
        if (nama.isBlank()) {
            inputNama.error = getString(R.string.error_profile_name)
            firstInvalid = etNama
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.error = getString(R.string.error_email_invalid)
            firstInvalid = firstInvalid ?: etEmail
        }
        if (telepon.count(Char::isDigit) < 9) {
            inputTelepon.error = getString(R.string.error_profile_phone)
            firstInvalid = firstInvalid ?: etTelepon
        }
        firstInvalid?.let {
            it.requestFocus()
            return
        }

        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(etEmail.windowToken, 0)

        val saveButton = findViewById<View>(R.id.tombolSimpanProfil)
        saveButton.isEnabled = false
        AuthStore.updateProfile(
            context = this,
            name = nama,
            email = email,
            phone = telepon,
            city = kota,
            bio = bio,
            avatarUri = selectedAvatarUri,
        ) { result ->
            saveButton.isEnabled = true
            result.fold(
                onSuccess = {
                    AppNotice.deferSuccess(this, R.string.profile_saved)
                    finish()
                },
                onFailure = { throwable ->
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
}
