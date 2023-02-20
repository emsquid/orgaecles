package fr.em.orgaecls.models

import java.util.*

class TaskModel(
    val uid: String = UUID.randomUUID().toString(),
    val campUid: String = "UID du camp",
    val name: String = "",
    val animator: String = ""
)