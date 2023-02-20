package fr.em.orgaecls.adapters

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import fr.em.orgaecls.CampActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.builders.buildConfirmDialog
import fr.em.orgaecls.databases.TasksRepository
import fr.em.orgaecls.models.TaskModel


class TaskAdapter(
    private val context: CampActivity,
    private val taskList: List<TaskModel>
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private var taskMutList = taskList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName = view.findViewById<EditText>(R.id.card_task_name)
        val taskAnimator = view.findViewById<EditText>(R.id.card_task_animator)
        val taskDeleteButton = view.findViewById<ImageView>(R.id.card_task_delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_task, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTask = taskMutList[position]
        // setup task name
        holder.taskName.setText(currentTask.name)
        holder.taskName.setOnEditorActionListener OnKeyListener@{ view, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
                updateTask(currentTask, holder)
                view.clearFocus()
                return@OnKeyListener true
            }
            false
        }
        // setup task anim
        holder.taskAnimator.setText(currentTask.animator)
        holder.taskAnimator.setOnEditorActionListener OnKeyListener@{ view, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
                updateTask(currentTask, holder)
                view.clearFocus()
                return@OnKeyListener true
            }
            false
        }

        // setup task delete
        holder.taskDeleteButton.setOnClickListener {
            buildConfirmDialog(context) {
                deleteItem(currentTask.uid)
                TasksRepository().deleteTask(currentTask.uid, currentTask.campUid)
            }
        }
    }

    private fun updateTask(taskBefore: TaskModel, holder: ViewHolder) {
        val newName = holder.taskName.text.toString()
        val newAnim = holder.taskAnimator.text.toString()
        if (newName != taskBefore.name || newAnim != taskBefore.animator) {
            val updatedTask =
                TaskModel(
                    taskBefore.uid,
                    taskBefore.campUid,
                    newName,
                    newAnim
                )
            TasksRepository().updateTask(updatedTask)
        }
    }

    override fun getItemCount(): Int = taskMutList.size

    private fun getItemPosition(taskUid: String): Int {
        for (position in 0 until itemCount) {
            if (taskMutList[position].uid == taskUid) {
                return position
            }
        }
        return -1
    }

    fun insertItem(task: TaskModel) {
        taskMutList.add(task)
        notifyItemInserted(itemCount - 1)
    }

    fun updateItem(task: TaskModel) {
        val position = getItemPosition(task.uid)
        if (position >= 0) {
            taskMutList[position] = task
            notifyItemChanged(position)
        } else {
            insertItem(task)
        }
    }

    fun deleteItem(childUid: String) {
        val position = getItemPosition(childUid)
        if (position >= 0) {
            taskMutList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}