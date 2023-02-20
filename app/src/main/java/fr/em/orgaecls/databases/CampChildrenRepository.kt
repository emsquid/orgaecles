package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.CampChildrenRepository.Singleton.databaseRef
import fr.em.orgaecls.models.ChildModel

class CampChildrenRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("campChildren")
    }

    lateinit var campWatchedUid: String
    lateinit var listener: ValueEventListener

    val childUidList = arrayListOf<String>()

    fun watchCampChildren(campUid: String, callback: () -> Unit) {
        campWatchedUid = campUid
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                childUidList.clear()
                for (child in snapshot.children) {
                    val uid = child.key
                    if (uid != null) {
                        childUidList.add(uid)
                    }
                }
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        databaseRef.child(campUid).addValueEventListener(listener)
    }

    fun stopWatchingCampChildren() {
        databaseRef.child(campWatchedUid).removeEventListener(listener)
    }

    fun addChildToCamp(child: ChildModel, campUid: String) =
        databaseRef.child("$campUid/${child.uid}").setValue(true)

    fun removeChildFromCamp(childUid: String, campUid: String) =
        databaseRef.child("$campUid/${childUid}").removeValue()
}