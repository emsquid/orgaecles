package fr.em.orgaecls.models

import java.util.*

class ChildModel(
    val uid: String = UUID.randomUUID().toString(),
    val campUid: String = "UID du camp",
    val firstName: String = "Prénom",
    val name: String = "Nom",
    val age: Int = 10,
    val autonomy: String = "Très Bon",
    val enuresis: String = "Oui",
    val encopresis: String = "Oui",
    val money: String = "Oui",
    val mail: String = "Oui",
    val medications: Map<String, String> = mapOf(),
    val diets: Map<String, Boolean> = mapOf(),
    val allergies: Map<String, Boolean> = mapOf(),
    val phobias: Map<String, Boolean> = mapOf(),
    val notes: String = ""
)