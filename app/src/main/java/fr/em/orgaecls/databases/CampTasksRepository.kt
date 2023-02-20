package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.CampTasksRepository.Singleton.databaseRef
import fr.em.orgaecls.models.TaskModel

class CampTasksRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("campTasks")
    }

    lateinit var campWatchedUid: String
    private lateinit var listener: ValueEventListener

    val taskUidList = arrayListOf<String>()

    fun watchCampTasks(campUid: String, callback: () -> Unit) {
        campWatchedUid = campUid
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskUidList.clear()
                for (child in snapshot.children) {
                    val uid = child.key
                    if (uid != null) {
                        taskUidList.add(uid)
                    }
                }
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        databaseRef.child(campUid).addValueEventListener(listener)
    }

    fun stopWatchingCampTasks() {
        databaseRef.child(campWatchedUid).removeEventListener(listener)
    }

    fun addTaskToCamp(task: TaskModel, campUid: String) =
        databaseRef.child("$campUid/${task.uid}").setValue(true)

    fun removeTaskFromCamp(taskUid: String, campUid: String) =
        databaseRef.child("$campUid/${taskUid}").removeValue()
}