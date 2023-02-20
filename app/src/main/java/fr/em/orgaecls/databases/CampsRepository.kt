package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.CampsRepository.Singleton.campMap
import fr.em.orgaecls.databases.CampsRepository.Singleton.databaseRef
import fr.em.orgaecls.databases.CampsRepository.Singleton.listenerMap
import fr.em.orgaecls.models.CampModel

class CampsRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("camps")

        var listenerMap = mutableMapOf<String, ValueEventListener>()
        var campMap = mutableMapOf<String, CampModel?>()
    }

    fun watchCamp(campUid: String, callback: () -> Unit) {
        listenerMap[campUid] = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val camp = snapshot.getValue(CampModel::class.java)
                campMap[campUid] = camp
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        listenerMap[campUid]?.let { databaseRef.child(campUid).addValueEventListener(it) }
    }

    fun stopWatchingCamp(campUid: String) {
        listenerMap[campUid]?.let {
            databaseRef.child(campUid).removeEventListener(it)
            listenerMap.remove(campUid)
        }
    }

    fun stopWatchingAllCamps() {
        for (campUid in campMap.keys) stopWatchingCamp(campUid)
    }

    fun insertCamp(camp: CampModel) {
        databaseRef.child(camp.uid).setValue(camp)

        val accessRepo = CampAccessRepository()
        // add access to camp
        for (userEmail in camp.animators.keys.toList()) {
            if (userEmail.replace(',', '.') != camp.creatorEmail) {
                accessRepo.addRequestForCampToUser(userEmail, camp.uid)
            }
        }
        accessRepo.addAccessToCampForUser(camp.creatorEmail, camp.uid)
    }

    fun updateCamp(oldCamp: CampModel, newCamp: CampModel) {
        databaseRef.child(newCamp.uid).setValue(newCamp)

        val accessRepo = CampAccessRepository()
        // remove removed users
        for (userEmail in oldCamp.animators.keys.toList()) {
            if (!newCamp.animators.keys.contains(userEmail)) {
                accessRepo.removeAccessToCampFromUser(userEmail, newCamp.uid)
            }
        }
        // add added users
        for (userEmail in newCamp.animators.keys.toList()) {
            if (!oldCamp.animators.keys.contains(userEmail)) {
                accessRepo.addRequestForCampToUser(userEmail, newCamp.uid)
            }
        }
    }

    fun deleteCamp(camp: CampModel) {
        val childrenRepo = ChildrenRepository()
        // remove children
        childrenRepo.deleteChildrenFromCamp(camp.uid) {
            val tasksRepo = TasksRepository()
            // remove tasks
            tasksRepo.deleteTasksFromCamp(camp.uid) {
                val eventsRepo = EventsRepository()
                // remove events
                eventsRepo.deleteEventsFromCamp(camp.uid) {
                    val accessRepo = CampAccessRepository()
                    // remove ref to camp from users accesses
                    for (userEmail in arrayListOf(camp.creatorEmail) + camp.animators.keys.toList()) {
                        accessRepo.removeAccessToCampFromUser(userEmail, camp.uid)
                    }
                    // finally delete camp
                    databaseRef.child(camp.uid).removeValue()
                }
            }
        }
    }

    fun removeAnimatorFromCamp(email: String, campUid: String) {
        databaseRef.child("$campUid/animators/${email.replace('.', ',')}").removeValue()
    }
}

