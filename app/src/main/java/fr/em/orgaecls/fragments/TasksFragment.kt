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
import fr.em.orgaecls.databases.TasksRepository
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.models.TaskModel
import java.util.*


class TasksFragment(private val context: CampActivity, private val camp: CampModel) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onStart() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.tasks_recycler_view)
        recyclerView?.adapter = context.taskAdapter

        val addTaskButton = view?.findViewById<FloatingActionButton>(R.id.task_add_fab)
        addTaskButton?.setOnClickListener {
            val newTask = TaskModel(UUID.randomUUID().toString(), camp.uid)
            TasksRepository().insertTask(newTask)
        }

        super.onStart()
    }
}