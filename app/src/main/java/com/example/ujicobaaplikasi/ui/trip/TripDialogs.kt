package com.example.ujicobaaplikasi.ui.trip

import android.app.Activity
import android.app.DatePickerDialog
import android.view.WindowManager
import android.view.View
import android.widget.TextView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.TripItem
import com.example.ujicobaaplikasi.data.TripPlan
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TripDialogs {

    private val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun showPlanDialog(
        activity: Activity,
        trip: TripPlan?,
        onSaved: () -> Unit,
    ) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_trip_plan, null)
        val inputTitle = view.findViewById<TextInputLayout>(R.id.inputTripTitle)
        val etTitle = view.findViewById<TextInputEditText>(R.id.etTripTitle)
        val inputStart = view.findViewById<TextInputLayout>(R.id.inputTripStart)
        val etStart = view.findViewById<TextInputEditText>(R.id.etTripStart)
        val inputEnd = view.findViewById<TextInputLayout>(R.id.inputTripEnd)
        val etEnd = view.findViewById<TextInputEditText>(R.id.etTripEnd)
        val etNotes = view.findViewById<TextInputEditText>(R.id.etTripNotes)
        val errorView = view.findViewById<TextView>(R.id.textTripPlanError)

        etTitle.setText(trip?.title.orEmpty())
        etNotes.setText(trip?.notes.orEmpty())
        etStart.tag = trip?.startDate
        etEnd.tag = trip?.endDate
        etStart.setText(TripDateFormatter.display(trip?.startDate))
        etEnd.setText(TripDateFormatter.display(trip?.endDate))

        etStart.setOnClickListener {
            pickDate(activity, etStart.tag as String?, etStart)
        }
        etEnd.setOnClickListener {
            pickDate(activity, etEnd.tag as String? ?: etStart.tag as String?, etEnd)
        }

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(if (trip == null) R.string.trip_new else R.string.trip_edit)
            .setView(view)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                inputTitle.error = null
                inputEnd.error = null
                val title = etTitle.text?.toString()?.trim().orEmpty()
                if (title.isBlank()) {
                    inputTitle.error = activity.getString(R.string.admin_field_required)
                    etTitle.requestFocus()
                    return@setOnClickListener
                }
                val start = etStart.tag as String?
                val end = etEnd.tag as String?
                if (start != null && end != null && end < start) {
                    inputEnd.error = activity.getString(R.string.error_trip_date_order)
                    return@setOnClickListener
                }

                val button = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                button.isEnabled = false
                errorView.visibility = View.GONE
                AppData.saveTrip(
                    context = activity,
                    tripId = trip?.id,
                    title = title,
                    startDate = start,
                    endDate = end,
                    notes = etNotes.text?.toString()?.trim().orEmpty(),
                ) { result ->
                    button.isEnabled = true
                    result.fold(
                        onSuccess = {
                            dialog.dismiss()
                            AppNotice.success(activity, R.string.trip_saved)
                            onSaved()
                        },
                        onFailure = { throwable ->
                            errorView.text = throwable.message
                                ?: activity.getString(R.string.error_network)
                            errorView.visibility = View.VISIBLE
                        },
                    )
                }
            }
        }
        dialog.show()
    }

    fun showItemDialog(
        activity: Activity,
        tripId: Long,
        item: TripItem?,
        defaultDay: Int,
        onSaved: () -> Unit,
    ) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_trip_item, null)
        val inputDay = view.findViewById<TextInputLayout>(R.id.inputTripItemDay)
        val etDay = view.findViewById<TextInputEditText>(R.id.etTripItemDay)
        val etTime = view.findViewById<TextInputEditText>(R.id.etTripItemTime)
        val inputTitle = view.findViewById<TextInputLayout>(R.id.inputTripItemTitle)
        val etTitle = view.findViewById<TextInputEditText>(R.id.etTripItemTitle)
        val etNotes = view.findViewById<TextInputEditText>(R.id.etTripItemNotes)
        val errorView = view.findViewById<TextView>(R.id.textTripItemError)

        etDay.setText((item?.dayNumber ?: defaultDay.coerceAtLeast(1)).toString())
        etTime.setText(item?.timeLabel.orEmpty())
        etTitle.setText(item?.title.orEmpty())
        etNotes.setText(item?.notes.orEmpty())

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(if (item == null) R.string.trip_item_new else R.string.trip_item_edit)
            .setView(view)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                inputDay.error = null
                inputTitle.error = null
                val day = etDay.text?.toString()?.trim()?.toIntOrNull()
                if (day == null || day < 1) {
                    inputDay.error = activity.getString(R.string.admin_field_required)
                    etDay.requestFocus()
                    return@setOnClickListener
                }
                val title = etTitle.text?.toString()?.trim().orEmpty()
                if (title.isBlank()) {
                    inputTitle.error = activity.getString(R.string.admin_field_required)
                    etTitle.requestFocus()
                    return@setOnClickListener
                }

                val button = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                button.isEnabled = false
                errorView.visibility = View.GONE
                AppData.saveTripItem(
                    context = activity,
                    tripId = tripId,
                    itemId = item?.id,
                    dayNumber = day,
                    timeLabel = etTime.text?.toString()?.trim().orEmpty(),
                    title = title,
                    notes = etNotes.text?.toString()?.trim().orEmpty(),
                ) { result ->
                    button.isEnabled = true
                    result.fold(
                        onSuccess = {
                            dialog.dismiss()
                            AppNotice.success(activity, R.string.trip_item_saved)
                            onSaved()
                        },
                        onFailure = { throwable ->
                            errorView.text = throwable.message
                                ?: activity.getString(R.string.error_network)
                            errorView.visibility = View.VISIBLE
                        },
                    )
                }
            }
        }
        dialog.show()
    }

    private fun pickDate(
        activity: Activity,
        currentIso: String?,
        target: TextInputEditText,
    ) {
        val calendar = Calendar.getInstance()
        if (!currentIso.isNullOrBlank()) {
            runCatching { iso.parse(currentIso)?.let { calendar.time = it } }
        }
        DatePickerDialog(
            activity,
            { _, year, month, day ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }
                val value = iso.format(picked.time)
                target.tag = value
                target.setText(TripDateFormatter.display(value))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ).show()
    }
}
