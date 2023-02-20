package fr.em.orgaecls.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.popups.CampPopup

class CampsFragment(private val context: MainActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camps, container, false)
    }

    override fun onStart() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.camps_recycler_view)
        recyclerView?.adapter = context.campAdapter

        val addCampButton = view?.findViewById<FloatingActionButton>(R.id.camp_add_fab)
        addCampButton?.setOnClickListener { CampPopup(context, null).show() }

        super.onStart()
    }
}