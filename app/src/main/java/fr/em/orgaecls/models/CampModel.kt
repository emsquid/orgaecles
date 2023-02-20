package fr.em.orgaecls.models

import java.io.Serializable

class CampModel(
    val uid: String = "uid",
    val name: String = "Nom de la Colo",
    val start: String = "Début de la Colo",
    val end: String = "Fin de la Colo",
    val creatorEmail: String = "creatorUid",
    val animators: Map<String, String> = mapOf(),
) : Serializable