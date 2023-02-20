package fr.em.orgaecls

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fr.em.orgaecls.adapters.ChildAdapter
import fr.em.orgaecls.adapters.TaskAdapter
import fr.em.orgaecls.builders.buildConfirmDialog
import fr.em.orgaecls.databases.*
import fr.em.orgaecls.fragments.CampCalendarFragment
import fr.em.orgaecls.fragments.ChildrenFragment
import fr.em.orgaecls.fragments.TasksFragment
import fr.em.orgaecls.fragments.TypicalDayFragment
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.models.EventModel

class CampActivity : AppCompatActivity() {
    lateinit var user: FirebaseUser
    lateinit var camp: CampModel
    lateinit var title: TextView

    // children uid data
    private val campChildrenRepo = CampChildrenRepository()
    private val childrenUidList = arrayListOf<String>()

    // children data
    private val childrenRepo = ChildrenRepository()

    // tasks uid data
    private val campTasksRepo = CampTasksRepository()
    private val taskUidList = arrayListOf<String>()

    // tasks data
    private val tasksRepo = TasksRepository()

    // adapter
    val childAdapter = ChildAdapter(this, arrayListOf())
    val taskAdapter = TaskAdapter(this, arrayListOf())

    // events uid
    private val campEventsRepo = CampEventsRepository()
    private val eventUidList = arrayListOf<String>()

    // events data
    private val eventsRepo = EventsRepository()
    val eventMap = mutableMapOf<String, EventModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camp)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            user = currentUser
            camp = (intent.getSerializableExtra("camp") as? CampModel)!!

            setupActivity()
            loadChildrenFragment(camp, false)

            setupRepos()
            setupDrawerAndNav()
        } else {
            goToSplashScreenActivity()
        }
    }

    override fun onBackPressed() {
        goToMainActivity()
    }

    // setups
    private fun setupActivity() {
        title = findViewById(R.id.camp_title)
        findViewById<ImageView>(R.id.camp_back_button).setOnClickListener { goToMainActivity() }
    }

    private fun setupRepos() {
        campChildrenRepo.watchCampChildren(camp.uid) {
            handleChildrenUidListUpdate(campChildrenRepo.childUidList)
        }
        campTasksRepo.watchCampTasks(camp.uid) {
            handleTaskUidListUpdate(campTasksRepo.taskUidList)
        }
        campEventsRepo.watchCampEvents(camp.uid) {
            handleEventUidListUpdate(CampEventsRepository.Singleton.eventUidList)
        }
    }

    private fun setupDrawerAndNav() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.camp_layout)

        findViewById<ImageView>(R.id.camp_menu_button).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val navView = findViewById<NavigationView>(R.id.camp_nav_view)
        navView.setCheckedItem(R.id.camp_menu_children_item)
        navView.setNavigationItemSelectedListener {
            if (navView.checkedItem != it) {
                when (it.itemId) {
                    R.id.camp_menu_children_item -> loadChildrenFragment(camp, false)
                    R.id.camp_menu_tasks_item -> loadTasksFragment(camp, false)
                    R.id.camp_menu_calendar_item -> loadCalendarFragment(camp, false)
                    R.id.camp_menu_typical_day_item -> loadTypicalDayFragment(false)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        val headerView = navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.camp_menu_header_name).text = camp.name
        headerView.findViewById<TextView>(R.id.camp_menu_header_start).text = camp.start
        headerView.findViewById<TextView>(R.id.camp_menu_header_end).text = camp.end
        val quitButton = headerView.findViewById<ConstraintLayout>(R.id.camp_menu_header_quit)
        if (user.email == camp.creatorEmail) {
            quitButton.visibility = View.GONE
        } else {
            quitButton.setOnClickListener {
                buildConfirmDialog(this) {
                    CampAccessRepository().removeAccessToCampFromUser(user.email!!, camp.uid)
                    goToSplashScreenActivity()
                }
            }
        }
    }

    // utils
    private fun handleChildrenUidListUpdate(updatedChildrenUid: List<String>) {
        // put a listener on new children
        val newChildrenUid = updatedChildrenUid.filter { !childrenUidList.contains(it) }
        for (childUid in newChildrenUid) {
            childrenRepo.watchChild(childUid) {
                val child = childrenRepo.childMap[childUid]
                if (child != null) childAdapter.updateItem(child)
            }
        }
        // remove listener on removed children
        val removedChildrenUid = childrenUidList.filter { !updatedChildrenUid.contains(it) }
        for (childUid in removedChildrenUid) {
            childrenRepo.stopWatchingChild(childUid)
            childAdapter.deleteItem(childUid)
        }
        // update uid list
        childrenUidList.clear()
        childrenUidList.addAll(updatedChildrenUid)
    }

    private fun handleTaskUidListUpdate(updatedTaskUid: List<String>) {
        // put a listener on new tasks
        val newTaskUid = updatedTaskUid.filter { !taskUidList.contains(it) }
        for (taskUid in newTaskUid) {
            tasksRepo.watchTask(taskUid) {
                val task = tasksRepo.taskMap[taskUid]
                if (task != null) taskAdapter.updateItem(task)
            }
        }
        // remove listener on removed tasks
        val removedTaskUid = taskUidList.filter { !updatedTaskUid.contains(it) }
        for (taskUid in removedTaskUid) {
            tasksRepo.stopWatchingTask(taskUid)
            taskAdapter.deleteItem(taskUid)
        }
        // update uid list
        taskUidList.clear()
        taskUidList.addAll(updatedTaskUid)
    }

    private fun handleEventUidListUpdate(updatedEventUidList: List<String>) {
        // put a listener on new events
        val newEventsUid = updatedEventUidList.filter { !eventUidList.contains(it) }
        for (eventUid in newEventsUid) {
            eventsRepo.watchEvent(eventUid, null, camp.uid) {
                val event = EventsRepository.Singleton.eventMap[eventUid]
                if (event != null) eventMap[eventUid] = event
            }
        }
        // remove listener on removed events
        val removedEventsUid = eventUidList.filter { !updatedEventUidList.contains(it) }
        for (eventUid in removedEventsUid) {
            eventsRepo.stopWatchingEvent(eventUid)
            eventMap.remove(eventUid)
        }
        // update uidlist
        eventUidList.clear()
        eventUidList.addAll(updatedEventUidList)
    }

    private fun detachListeners() {
        campChildrenRepo.stopWatchingCampChildren()
        childrenUidList.clear()

        campTasksRepo.stopWatchingCampTasks()
        taskUidList.clear()

        childrenRepo.stopWatchingAllChildren()
        tasksRepo.stopWatchingAllTasks()

        campEventsRepo.stopWatchingCampEvents()
        eventsRepo.stopWatchingEvents(eventUidList)
        eventUidList.clear()
    }

    // activity/fragments loader
    private fun goToMainActivity() {
        detachListeners()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }

    fun goToSplashScreenActivity() {
        detachListeners()
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun loadChildrenFragment(camp: CampModel, addToBackStack: Boolean) {
        title.text = resources.getText(R.string.camp_menu_children)
        loadFragment(ChildrenFragment(this, camp), addToBackStack)
    }

    private fun loadTasksFragment(camp: CampModel, addToBackStack: Boolean) {
        title.text = resources.getText(R.string.camp_menu_tasks)
        loadFragment(TasksFragment(this, camp), addToBackStack)
    }

    private fun loadCalendarFragment(camp: CampModel, addToBackStack: Boolean) {
        title.text = resources.getText(R.string.camp_menu_calendar)
        loadFragment(CampCalendarFragment(this, camp), addToBackStack)
    }

    private fun loadTypicalDayFragment(addToBackStack: Boolean) {
        title.text = resources.getText(R.string.camp_menu_typical_day)
        loadFragment(TypicalDayFragment(this), addToBackStack)
    }
}
