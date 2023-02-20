package fr.em.orgaecls.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.em.orgaecls.CampActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.calendar.DayView
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.popups.EventPopup
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class CampCalendarFragment(
    private val context: CampActivity,
    private val camp: CampModel
) : Fragment() {
    val user = context.user

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
    private val today = LocalDate.now()
    private var currentDay = LocalDate.now()
    private val start = LocalDate.parse(camp.start, dateFormatter)
    private val end = LocalDate.parse(camp.end, dateFormatter)

    private lateinit var dayView: DayView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camp_calendar, container, false)
    }

    override fun onStart() {
        dayView = view?.findViewById(R.id.camp_calendar_day_view)!!
        dayView.init(context, camp)

        if (currentDay.isBefore(start)) {
            loadDay(start)
        } else if (currentDay.isAfter(end)) {
            loadDay(end)
        } else {
            loadDay(currentDay)
        }

        view?.findViewById<TextView>(R.id.camp_calendar_current_day)?.setOnClickListener {
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

        val dayText = view?.findViewById<TextView>(R.id.camp_calendar_current_day)
        dayText?.text = stringDate

        val previousButton = view?.findViewById<ImageView>(R.id.camp_calendar_previous)
        if (currentDay.isAfter(start)) {
            previousButton?.visibility = View.VISIBLE
            previousButton?.setOnClickListener {
                decrementDateByOne()
            }
        } else {
            previousButton?.visibility = View.INVISIBLE
        }

        val nextButton = view?.findViewById<ImageView>(R.id.camp_calendar_next)
        if (currentDay.isBefore(end)) {
            nextButton?.visibility = View.VISIBLE
            nextButton?.setOnClickListener {
                incrementDateByOne()
            }
        } else {
            nextButton?.visibility = View.INVISIBLE
        }

        val addFab = view?.findViewById<FloatingActionButton>(R.id.camp_calendar_event_add_fab)
        addFab?.setOnClickListener {
            EventPopup(context, dayView, null, camp, true).show()
        }
    }

    private fun incrementDateByOne() {
        loadDay(currentDay.plusDays(1))
    }

    private fun decrementDateByOne() {
        loadDay(currentDay.minusDays(1))
    }

    private fun getCalendarConstraints(): CalendarConstraints {
        val dateValidatorMin: CalendarConstraints.DateValidator =
            DateValidatorPointForward.from(
                start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        val dateValidatorMax: CalendarConstraints.DateValidator =
            DateValidatorPointBackward.before(
                end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )

        val listValidators = ArrayList<CalendarConstraints.DateValidator>()
        listValidators.apply {
            add(dateValidatorMin)
            add(dateValidatorMax)
        }
        val validators = CompositeDateValidator.allOf(listValidators)

        return CalendarConstraints.Builder()
            .setValidator(validators)
            .build()
    }

    private fun addDatePicker() {
        val materialDatePickerBuilder =
            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(getCalendarConstraints())
        val datePicker = materialDatePickerBuilder.build()
        datePicker.addOnPositiveButtonClickListener {
            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            loadDay(date)
        }
        datePicker.show(context.supportFragmentManager, datePicker.toString())
    }
}