package fr.em.orgaecls.calendar

import android.graphics.Rect
import fr.em.orgaecls.models.EventModel

class EventRect(
    val event: EventModel,
    val rect: Rect,
    val top: Float,
    val bottom: Float,
    val left: Float,
    val right: Float,
    val disp: String,
    val color: String,
)
