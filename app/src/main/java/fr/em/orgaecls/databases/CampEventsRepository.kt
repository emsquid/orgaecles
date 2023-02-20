package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.CampEventsRepository.Singleton.databaseRef
import fr.em.orgaecls.databases.CampEventsRepository.Singleton.eventUidList
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.models.EventModel

class CampEventsRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("campEvents")

        val eventUidList = arrayListOf<String>()
    }

    private lateinit var campWatchedUid: String
    lateinit var listener: ValueEventListener

    fun watchCampEvents(campUid: String, callback: () -> Unit) {
        campWatchedUid = campUid
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventUidList.clear()
                for (child in snapshot.children) {
                    val uid = child.key
                    if (uid != null) {
                        eventUidList.add(uid)
                    }
                }
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        databaseRef.child(campWatchedUid).addValueEventListener(listener)
    }

    fun stopWatchingCampEvents() =
        databaseRef.child(campWatchedUid).removeEventListener(listener)

    fun addEventToCamp(event: EventModel, campUid: String) =
        databaseRef.child("$campUid/${event.uid}").setValue(true)

    fun removeEventFromCamp(eventUid: String, campUid: String) =
        databaseRef.child("$campUid/${eventUid}").removeValue()
}