package com.example.ujicobaaplikasi.ui.admin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.Catalog
import com.example.ujicobaaplikasi.data.ImageTools
import com.example.ujicobaaplikasi.data.MediaRepository
import com.example.ujicobaaplikasi.ui.adapters.ImageChoice
import com.example.ujicobaaplikasi.ui.adapters.ImageChoiceAdapter
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import java.util.concurrent.Executors

class AdminImagePickerActivity : BaseActivity() {

    private lateinit var adapter: ImageChoiceAdapter
    private lateinit var loading: View
    private lateinit var status: TextView
    private var selectedKey = ""
    private var uploadedChoices: List<ImageChoice> = emptyList()
    private val worker = Executors.newSingleThreadExecutor()

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) upload(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_image_picker)

        selectedKey = normalizeKey(intent.getStringExtra(EXTRA_CURRENT_KEY).orEmpty())
        findViewById<View>(R.id.imagePickerHeader).applyStatusBarInset()
        findViewById<View>(R.id.tombolKembaliImagePicker).setOnClickListener { finish() }
        findViewById<View>(R.id.tombolTanpaGambar).setOnClickListener { returnSelection("") }
        findViewById<View>(R.id.tombolUnggahGambar).setOnClickListener {
            filePicker.launch(arrayOf("image/*"))
        }

        loading = findViewById(R.id.imagePickerLoading)
        status = findViewById(R.id.imagePickerStatus)
        status.text = getString(R.string.admin_image_hint)
        findViewById<View>(R.id.imagePickerEmpty).visibility = View.GONE

        val recycler = findViewById<RecyclerView>(R.id.imagePickerRecycler)
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.itemAnimator = null
        adapter = ImageChoiceAdapter(selectedKey) { choice ->
            returnSelection(choice.key)
        }
        recycler.adapter = adapter

        loadMedia()
    }

    private fun loadMedia() {
        loading.visibility = View.VISIBLE
        MediaRepository.list { result ->
            loading.visibility = View.GONE
            result.fold(
                onSuccess = { media ->
                    uploadedChoices = media.map { item ->
                        ImageChoice(
                            key = MediaRepository.keyFor(item.id),
                            label = item.name,
                            sourceLabel = getString(R.string.image_picker_uploaded),
                            thumbnail = item.thumbnail,
                        )
                    }
                },
                onFailure = {
                    AppNotice.error(
                        this,
                        it.message ?: getString(R.string.error_network),
                    )
                },
            )
            renderChoices()
        }
    }

    private fun renderChoices() {
        val choices = uploadedChoices + Catalog.imageOptions.map { option ->
            ImageChoice(
                key = "builtin:${option.value}",
                label = option.label,
                sourceLabel = getString(R.string.image_picker_builtin),
                drawableRes = Catalog.image(option.value),
            )
        }
        adapter.submitList(choices)
        status.text = if (selectedKey.isNotBlank()) {
            getString(R.string.image_picker_selected)
        } else {
            getString(R.string.admin_image_hint)
        }
    }

    private fun upload(uri: Uri) {
        setBusy(true)
        worker.execute {
            val prepared = runCatching { ImageTools.prepare(this, uri) }
            runOnUiThread {
                prepared.fold(
                    onSuccess = { image ->
                        MediaRepository.upload(this, image) { result ->
                            setBusy(false)
                            result.fold(
                                onSuccess = { media ->
                                    AppNotice.deferSuccess(
                                        this,
                                        R.string.image_picker_uploaded_success,
                                    )
                                    returnSelection(MediaRepository.keyFor(media.id))
                                },
                                onFailure = {
                                    AppNotice.error(
                                        this,
                                        it.message ?: getString(R.string.error_network),
                                    )
                                },
                            )
                        }
                    },
                    onFailure = {
                        setBusy(false)
                        AppNotice.error(this, R.string.image_picker_upload_failed)
                    },
                )
            }
        }
    }

    private fun setBusy(value: Boolean) {
        loading.visibility = if (value) View.VISIBLE else View.GONE
        findViewById<View>(R.id.tombolUnggahGambar).isEnabled = !value
        findViewById<View>(R.id.tombolTanpaGambar).isEnabled = !value
        status.text = if (value) {
            getString(R.string.image_picker_processing)
        } else {
            getString(R.string.admin_image_hint)
        }
    }

    private fun returnSelection(key: String) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(EXTRA_SELECTED_KEY, key),
        )
        finish()
    }

    private fun normalizeKey(value: String): String = when {
        value.isBlank() -> ""
        value.startsWith("media:") || value.startsWith("builtin:") -> value
        else -> "builtin:$value"
    }

    override fun onDestroy() {
        super.onDestroy()
        worker.shutdownNow()
    }

    companion object {
        const val EXTRA_CURRENT_KEY = "extra_current_key"
        const val EXTRA_SELECTED_KEY = "extra_selected_key"

        fun intent(context: Context, currentKey: String): Intent =
            Intent(context, AdminImagePickerActivity::class.java)
                .putExtra(EXTRA_CURRENT_KEY, currentKey)
    }
}
