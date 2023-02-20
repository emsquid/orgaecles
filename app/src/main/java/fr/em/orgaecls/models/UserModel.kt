package fr.em.orgaecls.models

import java.io.Serializable

class UserModel(
    val uid: String = "0",
    val email: String = "example@gmail.com",
    val name: String = "Prénom",
    var campsUid: Map<String, Boolean> = mapOf()
) : Serializable