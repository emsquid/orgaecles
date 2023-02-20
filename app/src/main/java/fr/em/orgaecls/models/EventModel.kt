package fr.em.orgaecls.models

import java.util.*

data class EventModel(
    var name: String = "",
    var start: String = "00:00",
    var end: String = "00:00",
    val color: String = "green", // can be green/red/yellow/blue
    val description: String = "",
    val date: String = "2-7-2022",
    val uid: String = UUID.randomUUID().toString(),
    val ownerUid: String = "",
)

