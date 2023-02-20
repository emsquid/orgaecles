package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.EventsRepository.Singleton.databaseRef
import fr.em.orgaecls.databases.EventsRepository.Singleton.eventMap
import fr.em.orgaecls.models.EventModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventsRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("events")

        var eventMap = mutableMapOf<String, EventModel>()
    }

    var listenerMap = mutableMapOf<String, ValueEventListener>()

    fun watchEvent(eventUid: String, userUid: String?, campUid: String?, callback: () -> Unit) {
        listenerMap[eventUid] = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val event = snapshot.getValue(EventModel::class.java)
                if (event != null) {
                    if (eventShouldBeDeleted(event)) {
                        deleteEvent(event.uid, userUid, campUid)
                    } else {
                        eventMap[eventUid] = event
                    }
                }
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        listenerMap[eventUid]?.let { databaseRef.child(eventUid).addValueEventListener(it) }
    }

    fun stopWatchingEvent(eventUid: String) {
        listenerMap[eventUid]?.let {
            databaseRef.child(eventUid).removeEventListener(it)
        }
    }

    fun stopWatchingEvents(eventUidList: ArrayList<String>) {
        for (eventUid in eventUidList) stopWatchingEvent(eventUid)
    }

    fun insertEvent(event: EventModel, userUid: String?, campUid: String?) {
        databaseRef.child(event.uid).setValue(event)

        if (userUid != null) {
            val userEventsRepo = UserEventsRepository()
            // remove event from user
            userEventsRepo.addEventToUser(event, userUid)
        } else if (campUid != null) {
            val campEventsRepo = CampEventsRepository()
            // remove event from camp
            campEventsRepo.addEventToCamp(event, campUid)
        }
    }

    fun updateEvent(event: EventModel) {
        databaseRef.child(event.uid).setValue(event)
    }

    fun deleteEvent(eventUid: String, userUid: String?, campUid: String?) {
        if (userUid != null) {
            val userEventsRepo = UserEventsRepository()
            // remove event from user
            userEventsRepo.removeEventFromUser(eventUid, userUid)
        } else if (campUid != null) {
            val campEventsRepo = CampEventsRepository()
            // remove event from camp
            campEventsRepo.removeEventFromCamp(eventUid, campUid)
        }
        databaseRef.child(eventUid).removeValue()
    }

    fun deleteEventsFromCamp(campUid: String, callback: () -> Unit) {
        val campEventsRepo = CampEventsRepository()

        campEventsRepo.watchCampEvents(campUid) {
            campEventsRepo.stopWatchingCampEvents()

            for (eventUid in CampEventsRepository.Singleton.eventUidList) {
                deleteEvent(eventUid, null, campUid)
            }

            callback()
        }
    }

    private fun eventShouldBeDeleted(event: EventModel): Boolean {
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        val date = LocalDate.parse(event.date, dateFormatter)

        return date.isBefore(LocalDate.now().minusDays(30))
    }

}