package fr.em.orgaecls.databases

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.em.orgaecls.databases.UsersRepository.Singleton.databaseRef
import fr.em.orgaecls.databases.UsersRepository.Singleton.listener
import fr.em.orgaecls.databases.UsersRepository.Singleton.user
import fr.em.orgaecls.databases.UsersRepository.Singleton.userNameMap
import fr.em.orgaecls.models.UserModel

class UsersRepository {
    object Singleton {
        val databaseRef =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")

        var listener: ValueEventListener? = null
        var user: UserModel? = null

        var userNameMap = mutableMapOf<String, String>()
    }


    fun watchUser(userEmail: String, callback: () -> Unit) {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newUser = snapshot.getValue(UserModel::class.java)
                if (newUser != null) {
                    user = newUser
                }
                callback()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        listener?.let {
            databaseRef.child(userEmail.replace('.', ','))
                .addListenerForSingleValueEvent(it)
        }
    }

    fun stopWatchingUser() {
        listener?.let { listener ->
            {
                user?.let { user ->
                    {
                        databaseRef.child(
                            user.email.replace(
                                '.',
                                ','
                            )
                        ).removeEventListener(listener)
                    }
                }
            }
        }
    }

    fun insertUser(user: UserModel) =
        databaseRef.child(user.email.replace('.', ',')).setValue(user)

    fun fetchUserName(userEmail: String) {
        val email = userEmail.replace('.', ',')
        if (userNameMap[userEmail] == null) {
            databaseRef.child("$email/name").get().addOnSuccessListener {
                val name = it.getValue(String::class.java)
                if (name != null) userNameMap[email] = name
            }
        }
    }
}

