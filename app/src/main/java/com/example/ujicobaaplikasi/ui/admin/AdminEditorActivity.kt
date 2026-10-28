package com.example.ujicobaaplikasi.ui.admin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AdminEntity
import com.example.ujicobaaplikasi.data.AdminField
import com.example.ujicobaaplikasi.data.AdminFieldType
import com.example.ujicobaaplikasi.data.AdminOption
import com.example.ujicobaaplikasi.data.AdminRepository
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.data.Catalog
import com.example.ujicobaaplikasi.data.ImageTools
import com.example.ujicobaaplikasi.data.MediaRepository
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AdminEditorActivity : BaseActivity() {

    private lateinit var entity: AdminEntity
    private var recordId: String? = null
    private lateinit var form: LinearLayout
    private lateinit var loading: View
    private lateinit var saveButton: MaterialButton
    private val textInputs = linkedMapOf<String, TextInputEditText>()
    private val textLayouts = linkedMapOf<String, TextInputLayout>()
    private val optionValues = linkedMapOf<String, String>()
    private val imagePreviews = linkedMapOf<String, ImageView>()
    private val imageLabels = linkedMapOf<String, TextView>()
    private var destinationOptions: List<AdminOption> = emptyList()
    private var imageTargetColumn: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val column = imageTargetColumn ?: return@registerForActivityResult
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val key = result.data?.getStringExtra(AdminImagePickerActivity.EXTRA_SELECTED_KEY)
            ?: return@registerForActivityResult
        optionValues[column] = key
        renderImage(column, key)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entity = AdminEntity.entries.firstOrNull {
            it.key == intent.getStringExtra(EXTRA_ENTITY)
        } ?: run {
            finish()
            return
        }
        recordId = intent.getStringExtra(EXTRA_ID)?.takeIf { it.isNotBlank() }
        if (AuthStore.currentAccount(this)?.isAdmin != true) {
            finish()
            return
        }

        setContentView(R.layout.activity_admin_editor)
        findViewById<View>(R.id.adminEditorHeader).applyStatusBarInset()
        form = findViewById(R.id.adminEditorForm)
        loading = findViewById(R.id.adminEditorLoading)
        saveButton = findViewById(R.id.tombolSimpanAdmin)
        findViewById<View>(R.id.tombolKembaliAdminEditor).setOnClickListener { finish() }
        findViewById<TextView>(R.id.adminEditorTitle).text = buildString {
            append(entity.label)
            append(" · ")
            append(getString(if (recordId == null) R.string.admin_new else R.string.admin_edit))
        }
        saveButton.setOnClickListener { save() }

        if (entity.fields.any { it.type == AdminFieldType.DESTINATION }) {
            setLoading(true)
            AdminRepository.destinationOptions { result ->
                destinationOptions = result.getOrDefault(emptyList())
                loadRecord()
            }
        } else {
            loadRecord()
        }
    }

    private fun loadRecord() {
        val id = recordId
        if (id == null) {
            setLoading(false)
            buildForm(emptyMap())
            return
        }
        setLoading(true)
        AdminRepository.load(entity, id) { result ->
            setLoading(false)
            result.fold(
                onSuccess = { buildForm(it) },
                onFailure = {
                    AppNotice.error(
                        this,
                        it.message ?: getString(R.string.error_network),
                    )
                    finish()
                },
            )
        }
    }

    private fun buildForm(values: Map<String, String>) {
        form.removeAllViews()
        textInputs.clear()
        textLayouts.clear()
        optionValues.clear()
        imagePreviews.clear()
        imageLabels.clear()

        val density = resources.displayMetrics.density
        recordId?.let { id ->
            form.addView(
                TextView(this).apply {
                    text = "ID: $id"
                    setTextColor(getColor(R.color.teks_tersier))
                    textSize = 11f
                    setPadding(0, 0, 0, (10 * density).toInt())
                },
            )
        }

        entity.fields.forEach { field ->
            val raw = values[field.column].orEmpty()
            when (field.type) {
                AdminFieldType.IMAGE -> {
                    val value = normalizeImageValue(raw)
                    optionValues[field.column] = value
                    addImageField(field, value, density)
                }
                AdminFieldType.ICON,
                AdminFieldType.COLOR,
                AdminFieldType.ROLE,
                AdminFieldType.DESTINATION -> {
                    val value = raw.ifBlank { defaultOptionValue(field) }
                    optionValues[field.column] = value
                    addSelectField(field, value, density)
                }
                else -> addTextField(field, raw, density)
            }
        }
    }

    private fun addTextField(field: AdminField, value: String, density: Float) {
        val layout = TextInputLayout(this).apply {
            hint = labelWithRequired(field)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            helperText = field.hint.ifBlank { null }
            if (field.type == AdminFieldType.PASSWORD) {
                endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            }
        }
        val input = TextInputEditText(this).apply {
            setText(displayValue(field, value))
            maxLines = 1
            when (field.type) {
                AdminFieldType.MULTILINE, AdminFieldType.LIST -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    minLines = if (field.type == AdminFieldType.LIST) 2 else 4
                    gravity = Gravity.TOP
                    maxLines = if (field.type == AdminFieldType.LIST) 4 else 8
                }
                AdminFieldType.DECIMAL -> inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                AdminFieldType.INTEGER -> inputType = InputType.TYPE_CLASS_NUMBER
                AdminFieldType.PASSWORD -> inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                else -> inputType = when (field.column) {
                    "email" -> InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    "phone" -> InputType.TYPE_CLASS_PHONE
                    else -> InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                }
            }
            doAfterTextChanged { layout.error = null }
        }
        layout.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ),
        )
        textInputs[field.column] = input
        textLayouts[field.column] = layout
        form.addView(
            layout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = (12 * density).toInt() },
        )
    }

    private fun addSelectField(field: AdminField, value: String, density: Float) {
        val options = optionsFor(field)
        val layout = TextInputLayout(this).apply {
            hint = labelWithRequired(field)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            setEndIconDrawable(R.drawable.ic_chevron_right)
            setEndIconTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this@AdminEditorActivity, R.color.teks_tersier)),
            )
            helperText = field.hint.ifBlank { null }
        }
        val input = TextInputEditText(this).apply {
            inputType = InputType.TYPE_NULL
            isFocusable = false
            isClickable = true
            setText(options.firstOrNull { it.value == value }?.label ?: "Pilih")
        }
        layout.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ),
        )
        val openPicker = {
            val labels = options.map { it.label }.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setTitle(field.label)
                .setItems(labels) { _, which ->
                    val chosen = options[which]
                    optionValues[field.column] = chosen.value
                    input.setText(chosen.label)
                    updateOptionIcon(field, layout, chosen.value)
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
        input.setOnClickListener { openPicker() }
        layout.setOnClickListener { openPicker() }
        layout.setEndIconOnClickListener { openPicker() }
        textInputs[field.column] = input
        textLayouts[field.column] = layout
        updateOptionIcon(field, layout, value)
        form.addView(
            layout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = (12 * density).toInt() },
        )
    }

    private fun addImageField(field: AdminField, value: String, density: Float) {
        val card = MaterialCardView(this).apply {
            radius = 18 * density
            strokeWidth = (1 * density).toInt()
            strokeColor = ContextCompat.getColor(this@AdminEditorActivity, R.color.garis_lembut)
            setCardBackgroundColor(
                ContextCompat.getColor(this@AdminEditorActivity, R.color.permukaan),
            )
            cardElevation = 0f
            isClickable = true
            isFocusable = true
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (12 * density).toInt(),
                (12 * density).toInt(),
                (12 * density).toInt(),
                (12 * density).toInt(),
            )
        }
        val previewCard = MaterialCardView(this).apply {
            radius = 14 * density
            cardElevation = 0f
            setCardBackgroundColor(ContextCompat.getColor(this@AdminEditorActivity, R.color.permukaan_lembut))
        }
        val preview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = field.label
        }
        previewCard.addView(
            preview,
            android.widget.FrameLayout.LayoutParams(
                (68 * density).toInt(),
                (68 * density).toInt(),
                Gravity.CENTER,
            ),
        )
        row.addView(
            previewCard,
            LinearLayout.LayoutParams((68 * density).toInt(), (68 * density).toInt()),
        )

        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        textColumn.addView(
            TextView(this).apply {
                text = field.label
                setTextColor(ContextCompat.getColor(this@AdminEditorActivity, R.color.teks_utama))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                textSize = 13.5f
            },
        )
        val selectedLabel = TextView(this).apply {
            setTextColor(ContextCompat.getColor(this@AdminEditorActivity, R.color.hijau_lombok_gelap))
            textSize = 11.5f
            setPadding(0, (3 * density).toInt(), 0, 0)
        }
        textColumn.addView(selectedLabel)
        textColumn.addView(
            TextView(this).apply {
                text = field.hint.ifBlank { getString(R.string.admin_image_hint) }
                setTextColor(ContextCompat.getColor(this@AdminEditorActivity, R.color.teks_tersier))
                textSize = 9.5f
                setPadding(0, (2 * density).toInt(), 0, 0)
            },
        )
        row.addView(
            textColumn,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (12 * density).toInt()
            },
        )
        row.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.ic_chevron_right)
                imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this@AdminEditorActivity, R.color.teks_tersier),
                )
            },
            LinearLayout.LayoutParams((18 * density).toInt(), (18 * density).toInt()),
        )
        card.addView(row)
        card.setOnClickListener {
            imageTargetColumn = field.column
            imagePickerLauncher.launch(
                AdminImagePickerActivity.intent(
                    this,
                    optionValues[field.column].orEmpty(),
                ),
            )
        }

        imagePreviews[field.column] = preview
        imageLabels[field.column] = selectedLabel
        renderImage(field.column, value)
        form.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = (12 * density).toInt() },
        )
    }

    private fun renderImage(column: String, value: String) {
        val preview = imagePreviews[column] ?: return
        val label = imageLabels[column] ?: return
        val density = resources.displayMetrics.density
        when {
            value.startsWith("media:") -> {
                val id = MediaRepository.parseMediaId(value) ?: return
                label.text = "Memuat gambar…"
                preview.setImageResource(R.drawable.bg_lombok)
                preview.scaleType = ImageView.ScaleType.CENTER_CROP
                preview.imageTintList = null
                preview.setPadding(0, 0, 0, 0)
                MediaRepository.loadMeta(id) { result ->
                    val media = result.getOrNull()
                    if (media == null) {
                        renderImage(column, "")
                        return@loadMeta
                    }
                    ImageTools.bindThumbnail(preview, media.thumbnail, R.drawable.bg_lombok)
                    label.text = media.name
                }
            }
            value.startsWith("builtin:") -> {
                val key = value.removePrefix("builtin:")
                preview.setImageResource(Catalog.image(key))
                preview.scaleType = ImageView.ScaleType.CENTER_CROP
                preview.imageTintList = null
                preview.setPadding(0, 0, 0, 0)
                label.text = Catalog.imageLabel(key)
            }
            else -> {
                preview.setImageResource(R.drawable.ic_camera)
                preview.scaleType = ImageView.ScaleType.CENTER_INSIDE
                preview.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.hijau_lombok),
                )
                preview.setPadding(
                    (18 * density).toInt(),
                    (18 * density).toInt(),
                    (18 * density).toInt(),
                    (18 * density).toInt(),
                )
                label.text = getString(R.string.image_picker_none)
            }
        }
    }

    private fun updateOptionIcon(
        field: AdminField,
        layout: TextInputLayout,
        value: String,
    ) {
        when (field.type) {
            AdminFieldType.ICON -> {
                layout.setStartIconDrawable(Catalog.icon(value))
                layout.setStartIconTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hijau_lombok)),
                )
            }
            AdminFieldType.COLOR -> {
                layout.setStartIconDrawable(R.drawable.bg_color_dot)
                layout.setStartIconTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, Catalog.color(value))),
                )
            }
            AdminFieldType.ROLE -> {
                layout.setStartIconDrawable(R.drawable.ic_person)
                layout.setStartIconTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hijau_lombok)),
                )
            }
            AdminFieldType.DESTINATION -> {
                if (value.isBlank()) {
                    layout.setStartIconDrawable(null)
                } else {
                    layout.setStartIconDrawable(R.drawable.ic_location)
                    layout.setStartIconTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hijau_lombok)),
                    )
                }
            }
            else -> Unit
        }
    }

    private fun optionsFor(field: AdminField): List<AdminOption> = when (field.type) {
        AdminFieldType.ICON -> Catalog.iconOptions.map { AdminOption(it.value, it.label) }
        AdminFieldType.COLOR -> Catalog.colorOptions.map { AdminOption(it.value, it.label) }
        AdminFieldType.ROLE -> listOf(
            AdminOption("user", getString(R.string.role_user)),
            AdminOption("admin", getString(R.string.role_admin)),
        )
        AdminFieldType.DESTINATION -> listOf(
            AdminOption("", getString(R.string.admin_no_destination)),
        ) + destinationOptions
        else -> emptyList()
    }

    private fun defaultOptionValue(field: AdminField): String = when (field.type) {
        AdminFieldType.ROLE -> "user"
        AdminFieldType.ICON -> when (entity) {
            AdminEntity.LODGING -> "ic_menu_stay"
            AdminEntity.ACTIVITY -> "ic_menu_activity"
            AdminEntity.EVENT -> "ic_menu_event"
            AdminEntity.NOTIFICATION -> "ic_notifications"
            else -> "ic_menu_destination"
        }
        AdminFieldType.COLOR -> when (entity) {
            AdminEntity.LODGING -> if (field.column == "tint_key") "lavender" else "lavender_lembut"
            AdminEntity.ACTIVITY -> if (field.column == "tint_key") "hijau_lombok" else "hijau_lombok_muda"
            AdminEntity.EVENT -> if (field.column == "tint_key") "coklat_tua" else "pasir_kuning"
            AdminEntity.NOTIFICATION -> if (field.column == "tint_key") "hijau_lombok" else "hijau_lombok_muda"
            else -> "hijau_lombok"
        }
        else -> ""
    }

    private fun normalizeImageValue(value: String): String = when {
        value.isBlank() -> ""
        value.startsWith("media:") || value.startsWith("builtin:") -> value
        else -> "builtin:$value"
    }

    private fun labelWithRequired(field: AdminField): String =
        field.label + if (field.required) " *" else ""

    private fun displayValue(field: AdminField, value: String): String {
        return if (field.type == AdminFieldType.LIST) {
            TravelRepository.parseList(value).joinToString(", ")
        } else {
            value
        }
    }

    private fun save() {
        val values = linkedMapOf<String, String>()
        var firstInvalid: View? = null

        entity.fields.forEach { field ->
            textLayouts[field.column]?.error = null
            val value = when (field.type) {
                AdminFieldType.IMAGE,
                AdminFieldType.ICON,
                AdminFieldType.COLOR,
                AdminFieldType.ROLE,
                AdminFieldType.DESTINATION -> optionValues[field.column].orEmpty()
                else -> textInputs[field.column]?.text?.toString()?.trim().orEmpty()
            }
            val normalized = if (field.type == AdminFieldType.DECIMAL) {
                value.replace(',', '.')
            } else {
                value
            }
            values[field.column] = normalized

            if (field.required && value.isBlank()) {
                textLayouts[field.column]?.error = getString(R.string.admin_field_required)
                if (firstInvalid == null) firstInvalid = textInputs[field.column]
            }
            if (field.type == AdminFieldType.DECIMAL && normalized.isNotBlank()) {
                val number = normalized.toDoubleOrNull()
                if (number == null || number < 0.0 || number > 5.0) {
                    textLayouts[field.column]?.error = getString(R.string.admin_rating_error)
                    if (firstInvalid == null) firstInvalid = textInputs[field.column]
                }
            }
            if (field.type == AdminFieldType.INTEGER &&
                value.isNotBlank() && value.toIntOrNull() == null
            ) {
                textLayouts[field.column]?.error = getString(R.string.admin_field_required)
                if (firstInvalid == null) firstInvalid = textInputs[field.column]
            }
            if (entity == AdminEntity.USER && field.column == "email" &&
                value.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(value).matches()
            ) {
                textLayouts[field.column]?.error = getString(R.string.error_email_invalid)
                if (firstInvalid == null) firstInvalid = textInputs[field.column]
            }
            if (entity == AdminEntity.USER && field.column == "phone" &&
                value.isNotBlank() && value.count(Char::isDigit) < 9
            ) {
                textLayouts[field.column]?.error = getString(R.string.error_phone_invalid)
                if (firstInvalid == null) firstInvalid = textInputs[field.column]
            }
            if (entity == AdminEntity.USER && field.type == AdminFieldType.PASSWORD) {
                val passwordRequired = recordId == null
                if ((passwordRequired && value.isBlank()) ||
                    (value.isNotBlank() && value.length < 8)
                ) {
                    textLayouts[field.column]?.error = getString(R.string.error_password_short)
                    if (firstInvalid == null) firstInvalid = textInputs[field.column]
                }
            }
        }

        firstInvalid?.let {
            it.requestFocus()
            return
        }

        setLoading(true)
        AdminRepository.save(entity, recordId, values) { result ->
            setLoading(false)
            result.fold(
                onSuccess = {
                    AppData.clear()
                    AppNotice.deferSuccess(this, R.string.admin_saved)
                    finish()
                },
                onFailure = { throwable ->
                    val message = if (throwable.message?.contains("Duplicate entry") == true) {
                        getString(R.string.admin_duplicate)
                    } else {
                        throwable.message ?: getString(R.string.error_network)
                    }
                    AppNotice.error(this, message)
                },
            )
        }
    }

    private fun setLoading(value: Boolean) {
        loading.visibility = if (value) View.VISIBLE else View.GONE
        saveButton.isEnabled = !value
    }

    companion object {
        private const val EXTRA_ENTITY = "extra_entity"
        private const val EXTRA_ID = "extra_id"

        fun open(context: Context, entity: AdminEntity, id: String?) {
            context.startActivity(
                Intent(context, AdminEditorActivity::class.java)
                    .putExtra(EXTRA_ENTITY, entity.key)
                    .putExtra(EXTRA_ID, id),
            )
        }
    }
}
