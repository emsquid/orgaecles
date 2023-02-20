package fr.em.orgaecls.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R

class RequestsFragment(private val context: MainActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_requests, container, false)
    }

    override fun onStart() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.requests_recycler_view)
        recyclerView?.adapter = context.requestAdapter
        super.onStart()
    }
}