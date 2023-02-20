package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.TasksRepository.Singleton.databaseRef
import fr.em.orgaecls.models.TaskModel

class TasksRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("tasks")
    }

    var listenerMap = mutableMapOf<String, ValueEventListener>()
    var taskMap = mutableMapOf<String, TaskModel?>()

    fun watchTask(taskUid: String, callback: () -> Unit) {
        listenerMap[taskUid] = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val task = snapshot.getValue(TaskModel::class.java)
                taskMap[taskUid] = task
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        listenerMap[taskUid]?.let { databaseRef.child(taskUid).addValueEventListener(it) }
    }

    fun stopWatchingTask(taskUid: String) {
        listenerMap[taskUid]?.let { databaseRef.child(taskUid).removeEventListener(it) }
    }

    fun stopWatchingAllTasks() {
        for (taskUid in listenerMap.keys) stopWatchingTask(taskUid)
    }

    fun insertTask(task: TaskModel) {
        databaseRef.child(task.uid).setValue(task)

        val campTasksRepo = CampTasksRepository()
        // add task to camp
        campTasksRepo.addTaskToCamp(task, task.campUid)
    }

    fun updateTask(task: TaskModel) {
        databaseRef.child(task.uid).setValue(task)
    }

    fun deleteTask(taskUid: String, campUid: String) {
        val campTasksRepo = CampTasksRepository()
        // remove from camp
        campTasksRepo.removeTaskFromCamp(taskUid, campUid)

        databaseRef.child(taskUid).removeValue()
    }

    fun deleteTasksFromCamp(campUid: String, callback: () -> Unit) {
        val campTasksRepo = CampTasksRepository()
        // find tasks uid to delete them
        campTasksRepo.watchCampTasks(campUid) {
            // stop watching
            campTasksRepo.stopWatchingCampTasks()
            // delete tasks
            for (taskUid in campTasksRepo.taskUidList) {
                deleteTask(taskUid, campUid)
            }

            callback()
        }
    }
}