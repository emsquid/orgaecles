package fr.em.orgaecls.popups

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fr.em.orgaecls.R
import fr.em.orgaecls.builders.buildConfirmDialog
import fr.em.orgaecls.calendar.DayView
import fr.em.orgaecls.calendar.timeToFloat
import fr.em.orgaecls.databases.EventsRepository
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.models.EventModel
import java.util.*

internal class EventPopup(
    private val context: AppCompatActivity,
    private val dayView: DayView,
    private val event: EventModel?,
    private val camp: CampModel?,
    private val updatable: Boolean
) : Dialog(context) {
    private val user = Firebase.auth.currentUser
    private val calendar = Calendar.getInstance()
    private val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    private val currentMinute = calendar.get(Calendar.MINUTE)

    private var selectedColor = event?.color ?: "blue"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_event)
        setCanceledOnTouchOutside(false)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setupComponents()
        setupButtons()
    }

    private fun setupComponents() {
        findViewById<EditText>(R.id.popup_event_name).setText(event?.name)

        val startInput = findViewById<EditText>(R.id.popup_event_start)
        startInput
            .setText(
                event?.start ?: "${"%02d".format(currentHour)}:${"%02d".format(currentMinute)}"
            )
        if (updatable) setupTimePicker(startInput)

        val endInput = findViewById<EditText>(R.id.popup_event_end)
        endInput
            .setText(
                event?.end ?: "${"%02d".format((currentHour + 1) % 24)}:${
                    "%02d".format(
                        currentMinute
                    )
                }"
            )
        if (updatable) setupTimePicker(endInput)

        if (updatable) setupColorsPicker()

        findViewById<EditText>(R.id.popup_event_description).setText(event?.description)
    }

    @SuppressLint("SetTextI18n")
    private fun setupTimePicker(view: EditText) {
        fun addTimePicker(view: EditText) {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                view.setText("${"%02d".format(hour)}:${"%02d".format(minute)}")
            }

            TimePickerDialog(context, timeSetListener, currentHour, currentMinute, true).show()
            view.clearFocus()
        }

        view.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addTimePicker(view)
            }
        }

        view.setOnClickListener {
            addTimePicker(view)
        }
    }

    private fun setupColorsPicker() {
        val blue = findViewById<MaterialCardView>(R.id.event_color_blue)
        val green = findViewById<MaterialCardView>(R.id.event_color_green)
        val yellow = findViewById<MaterialCardView>(R.id.event_color_yellow)
        val red = findViewById<MaterialCardView>(R.id.event_color_red)

        val pickerMap = mapOf(
            Pair("blue", blue),
            Pair("green", green),
            Pair("yellow", yellow),
            Pair("red", red)
        )

        fun handleColorPick(pickedColor: String) {
            selectedColor = pickedColor
            for ((color, picker) in pickerMap) {
                if (color == pickedColor) {
                    picker.strokeWidth = 3
                } else {
                    picker.strokeWidth = 0
                }
            }
        }

        blue.setOnClickListener {
            handleColorPick("blue")
        }
        green.setOnClickListener {
            handleColorPick("green")
        }
        yellow.setOnClickListener {
            handleColorPick("yellow")
        }
        red.setOnClickListener {
            handleColorPick("red")
        }

        handleColorPick(selectedColor)
    }

    private fun setupButtons() {
        setupCloseButton()
        setupDeleteButton()
        setupSaveButton()
        if (!updatable) findViewById<View>(R.id.popup_event_separator).visibility = View.GONE
    }

    private fun setupCloseButton() {
        findViewById<ImageView>(R.id.popup_event_close_button).setOnClickListener {
            dismiss()
        }
    }

    private fun setupDeleteButton() {
        val deleteButton = findViewById<ImageView>(R.id.popup_event_delete_button)
        if (updatable) {
            deleteButton.setOnClickListener {
                if (event != null) {
                    buildConfirmDialog(context) {
                        dayView.deleteEvent(event.uid)
                        if (camp != null) {
                            EventsRepository().deleteEvent(event.uid, null, camp.uid)
                        } else {
                            EventsRepository().deleteEvent(event.uid, user?.uid, null)
                        }
                        dismiss()
                    }
                } else {
                    dismiss()
                }
            }
        } else {
            deleteButton.visibility = View.GONE
        }
    }

    private fun setupSaveButton() {
        val saveButton = findViewById<ImageView>(R.id.popup_event_save_button)
        if (updatable) {
            saveButton.setOnClickListener {
                val eventsRepo = EventsRepository()

                val name = findViewById<EditText>(R.id.popup_event_name).text.toString()
                val start = findViewById<EditText>(R.id.popup_event_start).text.toString()
                val end = findViewById<EditText>(R.id.popup_event_end).text.toString()
                val color = selectedColor
                val description =
                    findViewById<EditText>(R.id.popup_event_description).text.toString()

                if (name == "") {
                    toast("Un champ est incomplet")
                } else if (!hoursValid(start, end)) {
                    toast("Les horaires sont mal choisis")
                } else if (event == null) {
                    val newEvent = EventModel(
                        name,
                        start,
                        end,
                        color,
                        description,
                        dayView.date,
                        UUID.randomUUID().toString(),
                        camp?.uid ?: user!!.uid
                    )
                    dayView.insertEvent(newEvent)
                    eventsRepo.insertEvent(
                        newEvent,
                        if (camp == null) user?.uid else null,
                        camp?.uid
                    )
                    dismiss()
                } else {
                    val updatedEvent = EventModel(
                        name,
                        start,
                        end,
                        color,
                        description,
                        event.date,
                        event.uid,
                        event.ownerUid,
                    )
                    dayView.updateEvent(updatedEvent)
                    eventsRepo.updateEvent(updatedEvent)
                    dismiss()
                }

            }
        } else {
            saveButton.visibility = View.GONE
        }
    }

    private fun toast(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun hoursValid(start: String, end: String): Boolean {
        return timeToFloat(start) + 0.25 <= timeToFloat(end)
    }
}
