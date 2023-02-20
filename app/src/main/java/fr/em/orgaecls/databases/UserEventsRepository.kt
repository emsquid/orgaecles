package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.UserEventsRepository.Singleton.databaseRef
import fr.em.orgaecls.databases.UserEventsRepository.Singleton.eventUidList
import fr.em.orgaecls.models.EventModel

class UserEventsRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("userEvents")

        val eventUidList = arrayListOf<String>()
    }

    private lateinit var userWatchedUid: String
    lateinit var listener: ValueEventListener

    fun watchUserEvents(userUid: String, callback: () -> Unit) {
        userWatchedUid = userUid
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

        databaseRef.child(userWatchedUid).addValueEventListener(listener)
    }

    fun stopWatchingUserEvents() =
        databaseRef.child(userWatchedUid).removeEventListener(listener)

    fun addEventToUser(event: EventModel, userUid: String) =
        databaseRef.child("$userUid/${event.uid}").setValue(true)

    fun removeEventFromUser(eventUid: String, userUid: String) =
        databaseRef.child("$userUid/${eventUid}").removeValue()
}