package fr.em.orgaecls.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.em.orgaecls.CampActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.popups.ChildPopup

class ChildrenFragment(private val context: CampActivity, private val camp: CampModel) :
    Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_children, container, false)
    }

    override fun onStart() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.children_recycler_view)
        recyclerView?.adapter = context.childAdapter

        val addChildButton = view?.findViewById<FloatingActionButton>(R.id.child_add_fab)
        addChildButton?.setOnClickListener { ChildPopup(context, null, camp.uid).show() }
        super.onStart()
    }
}