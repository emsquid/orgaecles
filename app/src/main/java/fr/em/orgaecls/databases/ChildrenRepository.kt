package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.ChildrenRepository.Singleton.databaseRef
import fr.em.orgaecls.models.ChildModel

class ChildrenRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("children")
    }

    var listenerMap = mutableMapOf<String, ValueEventListener>()
    var childMap = mutableMapOf<String, ChildModel?>()

    fun watchChild(childUid: String, callback: () -> Unit) {
        listenerMap[childUid] = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val child = snapshot.getValue(ChildModel::class.java)
                childMap[childUid] = child
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        listenerMap[childUid]?.let { databaseRef.child(childUid).addValueEventListener(it) }
    }

    fun stopWatchingChild(childUid: String) {
        listenerMap[childUid]?.let { databaseRef.child(childUid).removeEventListener(it) }
    }

    fun stopWatchingAllChildren() {
        for (childUid in listenerMap.keys) stopWatchingChild(childUid)
    }

    fun insertChild(child: ChildModel) {
        databaseRef.child(child.uid).setValue(child)

        val campChildrenRepo = CampChildrenRepository()
        // add child to camp
        campChildrenRepo.addChildToCamp(child, child.campUid)
    }

    fun updateChild(child: ChildModel) {
        databaseRef.child(child.uid).setValue(child)
    }

    fun deleteChild(childUid: String, campUid: String) {
        val campChildrenRepo = CampChildrenRepository()
        // remove from camp
        campChildrenRepo.removeChildFromCamp(childUid, campUid)

        databaseRef.child(childUid).removeValue()
    }

    fun deleteChildrenFromCamp(campUid: String, callback: () -> Unit) {
        val campChildrenRepo = CampChildrenRepository()
        // find children uid to delete them
        campChildrenRepo.watchCampChildren(campUid) {
            // stop watching
            campChildrenRepo.stopWatchingCampChildren()
            // delete children
            for (childUid in campChildrenRepo.childUidList) {
                deleteChild(childUid, campUid)
            }

            callback()
        }
    }
}