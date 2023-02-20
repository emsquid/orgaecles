package fr.em.orgaecls.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.calendar.DayView
import fr.em.orgaecls.popups.EventPopup
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class UserCalendarFragment(private val context: MainActivity) : Fragment() {
    val user = context.user

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
    private val today = LocalDate.now()
    private var currentDay = LocalDate.now()

    private lateinit var dayView: DayView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_calendar, container, false)
    }

    override fun onStart() {
        dayView = view?.findViewById(R.id.user_calendar_day_view)!!
        dayView.init(context, null)

        loadDay(currentDay)
        view?.findViewById<ImageView>(R.id.user_calendar_previous)?.setOnClickListener {
            decrementDateByOne()
        }
        view?.findViewById<ImageView>(R.id.user_calendar_next)?.setOnClickListener {
            incrementDateByOne()
        }
        view?.findViewById<TextView>(R.id.user_calendar_current_day)?.setOnClickListener {
            addDatePicker()
        }

        super.onStart()
    }

    private fun loadDay(date: LocalDate) {
        currentDay = date
        val stringDate = dateFormatter.format(currentDay)
        // get corresponding events
        val events = ArrayList(context.eventMap.values.filter { it.date == stringDate })

        dayView.update(stringDate, events, true)

        val dayText = view?.findViewById<TextView>(R.id.user_calendar_current_day)
        dayText?.text = stringDate

        val addFab = view?.findViewById<FloatingActionButton>(R.id.user_calendar_event_add_fab)
        if (!currentDay.isBefore(today)) {
            addFab?.visibility = View.VISIBLE
            addFab?.setOnClickListener {
                EventPopup(context, dayView, null, null, true).show()
            }
        } else {
            addFab?.visibility = View.INVISIBLE
        }
    }

    private fun incrementDateByOne() {
        loadDay(currentDay.plusDays(1))
    }

    private fun decrementDateByOne() {
        loadDay(currentDay.minusDays(1))
    }

    private fun addDatePicker() {
        val materialDatePickerBuilder =
            MaterialDatePicker.Builder.datePicker()
        val datePicker = materialDatePickerBuilder.build()
        datePicker.addOnPositiveButtonClickListener {
            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            loadDay(date)
        }
        datePicker.show(context.supportFragmentManager, datePicker.toString())
    }
}