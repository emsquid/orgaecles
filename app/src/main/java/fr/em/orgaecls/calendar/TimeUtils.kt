package fr.em.orgaecls.calendar

fun timeToFloat(time: String): Float {
    val hm = time.split(":")
    val h = hm[0].toFloat()
    val m = hm[1].toFloat()
    return h + m / 60f
}
