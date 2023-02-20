package fr.em.orgaecls.databases

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.CampAccessRepository.Singleton.campAccessList
import fr.em.orgaecls.databases.CampAccessRepository.Singleton.campRequestList
import fr.em.orgaecls.databases.CampAccessRepository.Singleton.databaseRef
import fr.em.orgaecls.databases.CampAccessRepository.Singleton.listener
import fr.em.orgaecls.databases.CampAccessRepository.Singleton.userWatched

class CampAccessRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("campAccess")


        lateinit var userWatched: FirebaseUser
        lateinit var listener: ValueEventListener

        val campAccessList = arrayListOf<String>()
        val campRequestList = arrayListOf<String>()
    }

    fun watchCampAccess(user: FirebaseUser, callback: () -> Unit) {
        userWatched = user
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                campAccessList.clear()
                campRequestList.clear()
                for (child in snapshot.children) {
                    val uid = child.key
                    val access = child.getValue(Boolean::class.java)
                    if (uid != null && access != null) {
                        if (access) campAccessList.add(uid)
                        else campRequestList.add(uid)
                    }
                }
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        databaseRef.child(user.email!!.replace('.', ','))
            .addValueEventListener(listener)
    }

    fun stopWatchingCampAccess() {
        databaseRef.child(userWatched.email!!.replace('.', ',')).removeEventListener(listener)
    }

    fun addRequestForCampToUser(userEmail: String, campUid: String) =
        databaseRef.child("${userEmail.replace('.', ',')}/$campUid").setValue(false)

    fun addAccessToCampForUser(userEmail: String, campUid: String) =
        databaseRef.child("${userEmail.replace('.', ',')}/$campUid").setValue(true)

    fun removeAccessToCampFromUser(userEmail: String, campUid: String) {
        databaseRef.child("${userEmail.replace('.', ',')}/$campUid").removeValue()
        CampsRepository().removeAnimatorFromCamp(userEmail, campUid)
    }
}