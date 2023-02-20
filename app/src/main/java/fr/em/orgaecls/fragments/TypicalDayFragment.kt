package fr.em.orgaecls.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import fr.em.orgaecls.CampActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.calendar.DayView
import fr.em.orgaecls.models.EventModel

class TypicalDayFragment(val context: CampActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_typical_day, container, false)
    }

    override fun onStart() {
        val dayView = view?.findViewById<DayView>(R.id.typical_day_view)

        dayView?.init(context, context.camp)

        val events = arrayListOf(
            EventModel("Réveil", "07:15", "07:45", "blue"),
            EventModel("Petit déjeuner", "07:46", "09:20", "yellow"),
            EventModel("TL", "09:21", "10:30", "green"),
            EventModel("Activités", "10:31", "12:30", "red"),
            EventModel("Repas du midi", "12:31", "13:30", "yellow"),
            EventModel("TL", "13:31", "14:30", "green"),
            EventModel("Activités", "14:31", "16:30", "red"),
            EventModel("Goûté", "16:31", "17:00", "yellow"),
            EventModel("Tel/Douches/Petites activités", "17:01", "19:00", "blue"),
            EventModel("Repas du soir", "19:01", "20:30", "yellow"),
            EventModel("Veillée", "20:31", "22:30", "red")
        )

        dayView?.update("anyday", events, false)

        super.onStart()
    }
}