package fr.em.orgaecls.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import fr.em.orgaecls.R
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.models.EventModel
import fr.em.orgaecls.popups.EventPopup
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max


class DayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    lateinit var activity: AppCompatActivity
    var camp: CampModel? = null

    private val hourViews = ArrayList<View>()
    private val eventViews = ArrayList<View>()
    private val eventRects = ArrayList<EventRect>()

    private val events: ArrayList<EventModel> = ArrayList()
    var date: String = "2-7-2022"
    private var updatable: Boolean = true

    private val eventViewMargin = dpToPx(8)
    private val colorMap = mapOf(
        Pair("green", Color.parseColor("#35473A")),
        Pair("red", Color.parseColor("#8f0101")),
        Pair("yellow", Color.parseColor("#e0bb00")),
        Pair("blue", Color.parseColor("#00276E"))
    )

    private var mDetector: GestureDetectorCompat

    init {
        orientation = VERTICAL

        initHourViews()

        mDetector =
            GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

                override fun onDown(event: MotionEvent): Boolean {
                    return true
                }


                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    if (e != null) {
                        val i =
                            eventRects.indexOfFirst {
                                it.rect.contains(
                                    e.x.toInt(),
                                    e.y.toInt()
                                )
                            }
                        if (i != -1) {
                            eventViews[i].callOnClick()
                        }
                    }
                    return super.onSingleTapConfirmed(e)
                }
            })
    }

    fun init(activity: AppCompatActivity, camp: CampModel?) {
        this.activity = activity
        this.camp = camp
    }

    fun update(date: String, events: ArrayList<EventModel>, updatable: Boolean) {
        this.events.clear()
        this.events.addAll(events)
        this.date = date
        this.updatable = updatable

        onChange()
    }

    private fun getEventIndex(eventUid: String): Int {
        for (index in 0 until events.size) {
            if (events[index].uid == eventUid) {
                return index
            }
        }
        return -1
    }

    fun insertEvent(event: EventModel) {
        events.add(event)
        onChange()
    }

    fun updateEvent(event: EventModel) {
        val index = getEventIndex(event.uid)
        if (index >= 0) {
            events[index] = event
            onChange()
            Log.d("hey", "I should have updated now")
        } else {
            insertEvent(event)
        }
    }

    fun deleteEvent(eventUid: String) {
        val index = getEventIndex(eventUid)
        if (index >= 0) {
            events.removeAt(index)
            onChange()
            Log.d("hey", "I should have deleted now")
        }
    }

    private fun onChange() {
        sortEvents()
        eventRects.clear()
        computeEvents()
        addEventViews()
        requestLayout()
        invalidate()
    }

    private fun sortEvents() {
        events.sortBy { timeToFloat(it.start) }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val divWidth = hourViews[0].findViewById<View>(R.id.hour_divider).width
        eventViews.forEachIndexed { index, view ->
            val top = getPosFromIndex(eventRects[index].top)
            val bottom = getPosFromIndex(eventRects[index].bottom)
            val left =
                if (eventRects[index].left < 0.01) ((right - divWidth) + eventRects[index].left * divWidth).toInt()
                else ((right - eventViewMargin * 0.25 - divWidth) + eventRects[index].left * divWidth).toInt()
            val right =
                if (eventRects[index].right > 0.99) ((right - eventViewMargin * 2 - divWidth) + eventRects[index].right * divWidth).toInt()
                else ((right - eventViewMargin * 0.5 - divWidth) + eventRects[index].right * divWidth).toInt()

            handleViewSize(view, right - left, bottom - top)

            val widthSpec = MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY)
            val heightSpec = MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, right - left, bottom - top)
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        val divWidth = hourViews[0].findViewById<View>(R.id.hour_divider).width
        eventViews.forEachIndexed { index, view ->
            val top = getPosFromIndex(eventRects[index].top)
            val bottom = getPosFromIndex(eventRects[index].bottom)
            val left =
                if (eventRects[index].left < 0.01) ((right - divWidth) + eventRects[index].left * divWidth).toInt()
                else ((right - eventViewMargin * 0.25 - divWidth) + eventRects[index].left * divWidth).toInt()
            val right =
                if (eventRects[index].right > 0.99) ((right - eventViewMargin * 2 - divWidth) + eventRects[index].right * divWidth).toInt()
                else ((right - eventViewMargin * 0.5 - divWidth) + eventRects[index].right * divWidth).toInt()

            eventRects[index].rect.set(left, top, right, bottom)

            canvas?.save()
            canvas?.translate(left.toFloat(), top.toFloat())
            view.draw(canvas)
            canvas?.restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private fun initHourViews() {
        hourViews.clear()
        removeAllViewsInLayout()
        for (i in 0..24) {
            val view = LayoutInflater
                .from(context).inflate(R.layout.view_hour, this, false)
            view.findViewById<TextView>(R.id.hour_label).text = getTime(i)
            hourViews.add(view)

            this.addView(view)
        }
    }

    private fun addEventViews() {
        eventViews.clear()
        for (eventR in eventRects) {
            val view = LayoutInflater
                .from(context).inflate(R.layout.view_event, this, false)

            // set texts
            view.findViewById<TextView>(R.id.event_name).text = eventR.event.name
            view.findViewById<TextView>(R.id.event_time).text = eventR.disp
            // set color
            colorMap[eventR.color]?.let {
                view.findViewById<CardView>(R.id.card_view).setCardBackgroundColor(it)
            }

            eventViews.add(view)

            view.setOnClickListener {
                EventPopup(activity, this, eventR.event, camp, updatable).show()
            }
        }
    }

    private fun computeEvents() {
        val collisionGroups = arrayListOf<ArrayList<EventModel>>()
        for (event in events) {
            var isPlaced = false

            outerLoop@ for (group in collisionGroups) {
                for (gEvent in group) {
                    if (eventsCollide(event, gEvent)) {
                        group.add(event)
                        isPlaced = true
                        break@outerLoop
                    }
                }
            }

            if (!isPlaced) {
                val newGroup = arrayListOf(event)
                collisionGroups.add(newGroup)
            }
        }

        for (group in collisionGroups) {
            computeEventPos(group)
        }
    }

    private fun computeEventPos(collisionGroup: ArrayList<EventModel>) {
        val columns = arrayListOf(arrayListOf<EventModel>())
        for (event in collisionGroup) {
            var isPlaced = false

            for (column in columns) {
                if (column.size == 0) {
                    column.add(event)
                    isPlaced = true
                } else if (!eventsCollide(event, column.last())) {
                    column.add(event)
                    isPlaced = true
                    break
                }
            }

            if (!isPlaced) {
                val newColumn = arrayListOf(event)
                columns.add(newColumn)
            }
        }

        var maxCount = 0
        for (column in columns) {
            maxCount = max(maxCount, column.size)
        }
        for (i in 0 until maxCount) {
            for ((j, column) in columns.withIndex()) {
                if (column.size >= i + 1) {
                    val event = column[i]
                    val top = getTimeIndex(event.start)
                    val bottom = getTimeIndex(event.end)
                    val left = j.toFloat() / columns.size
                    val right = left + 1f / columns.size

                    val eventRect = EventRect(
                        event,
                        Rect(),
                        top,
                        bottom,
                        left,
                        right,
                        "${event.start} - ${event.end}",
                        event.color
                    )

                    this.eventRects.add(eventRect)
                }
            }
        }
    }

    private fun eventsCollide(event1: EventModel, event2: EventModel): Boolean {
        val start1 = getTimeIndex(event1.start)
        val end1 = getTimeIndex(event1.end)
        val start2 = getTimeIndex(event2.start)
        val end2 = getTimeIndex(event2.end)

        return !((start1 >= end2) || (end1 <= start2))
    }

    private fun handleViewSize(view: View, width: Int, height: Int) {
        if (height < 185) {
            view.findViewById<TextView>(R.id.event_time).visibility = View.INVISIBLE
        }
        if (height < 120) {
            view.findViewById<ConstraintLayout>(R.id.event_constraint)
                .setPadding(dpToPx(13), 2, 13, 2)
        }
        if (height < 60) {
            view.findViewById<TextView>(R.id.event_name).textSize = 13F
        }
    }

    private fun getTime(index: Int): String {
        return "${"%02d".format(index)}:00"
    }


    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()


    private fun getTimeIndex(time: String): Float {
        val hm = time.split(":")
        var h = hm[0].toFloat()
        val m = hm[1].toFloat()
        h += m / 60f

        if (h < 0) h += 24
        return h
    }

    private fun getPosFromIndex(index: Float): Int {
        val l = floor(index).toInt()
        val h = ceil(index).toInt()

        val lVal =
            hourViews[l].let { it.top + it.findViewById<View>(R.id.hour_divider).top }
        val hVal =
            hourViews[h].let { it.top + it.findViewById<View>(R.id.hour_divider).top }

        return (lVal + (index - l) * (hVal - lVal)).toInt()
    }
}