package com.example.ujicobaaplikasi.ui.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AdminEntity
import com.example.ujicobaaplikasi.data.AdminRecord
import com.example.ujicobaaplikasi.data.AdminRepository
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.ui.adapters.AdminRecordAdapter
import com.example.ujicobaaplikasi.ui.common.BaseActivity
import com.example.ujicobaaplikasi.ui.common.applyStatusBarInset
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminListActivity : BaseActivity() {

    private lateinit var entity: AdminEntity
    private lateinit var adapter: AdminRecordAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var loadingView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entity = AdminEntity.entries.firstOrNull {
            it.key == intent.getStringExtra(EXTRA_ENTITY)
        } ?: run {
            finish()
            return
        }
        if (AuthStore.currentAccount(this)?.isAdmin != true) {
            finish()
            return
        }

        setContentView(R.layout.activity_admin_list)
        findViewById<View>(R.id.adminListHeader).applyStatusBarInset()
        findViewById<TextView>(R.id.adminListTitle).text = entity.label
        findViewById<View>(R.id.tombolKembaliAdminList).setOnClickListener { finish() }
        findViewById<View>(R.id.tombolTambahAdmin).setOnClickListener {
            AdminEditorActivity.open(this, entity, null)
        }

        recycler = findViewById(R.id.adminListRecycler)
        emptyView = findViewById(R.id.adminListEmpty)
        loadingView = findViewById(R.id.adminListLoading)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.itemAnimator = null
        adapter = AdminRecordAdapter(
            onOpen = { record -> AdminEditorActivity.open(this, entity, record.id) },
            onDelete = ::confirmDelete,
        )
        recycler.adapter = adapter

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        loadingView.visibility = View.VISIBLE
        AdminRepository.list(entity) { result ->
            loadingView.visibility = View.GONE
            result.fold(
                onSuccess = { records ->
                    adapter.submitList(records)
                    emptyView.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
                    recycler.visibility = if (records.isEmpty()) View.GONE else View.VISIBLE
                },
                onFailure = {
                    AppNotice.error(
                        this,
                        it.message ?: getString(R.string.error_network),
                    )
                },
            )
        }
    }

    private fun confirmDelete(record: AdminRecord) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.admin_delete_title)
            .setMessage(getString(R.string.admin_delete_message, record.title))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                val userId = AuthStore.currentUserId(this) ?: 0
                AdminRepository.delete(entity, record.id, userId) { result ->
                    result.fold(
                        onSuccess = {
                            AppData.clear()
                            AppNotice.success(this, R.string.admin_deleted)
                            refresh()
                        },
                        onFailure = {
                            AppNotice.error(
                                this,
                                it.message ?: getString(R.string.error_network),
                            )
                        },
                    )
                }
            }
            .show()
    }

    companion object {
        private const val EXTRA_ENTITY = "extra_entity"

        fun open(context: Context, entity: AdminEntity) {
            context.startActivity(
                Intent(context, AdminListActivity::class.java)
                    .putExtra(EXTRA_ENTITY, entity.key),
            )
        }
    }
}
