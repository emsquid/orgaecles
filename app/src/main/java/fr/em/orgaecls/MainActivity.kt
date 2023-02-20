package fr.em.orgaecls

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fr.em.orgaecls.adapters.CampAdapter
import fr.em.orgaecls.adapters.RequestAdapter
import fr.em.orgaecls.builders.buildConfirmDialog
import fr.em.orgaecls.databases.*
import fr.em.orgaecls.fragments.CampsFragment
import fr.em.orgaecls.fragments.RequestsFragment
import fr.em.orgaecls.fragments.UserCalendarFragment
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.models.EventModel

class MainActivity : AppCompatActivity() {
    lateinit var user: FirebaseUser
    lateinit var title: TextView

    // events uid
    private val userEventsRepo = UserEventsRepository()
    private val eventUidList = arrayListOf<String>()

    // events data
    private val eventsRepo = EventsRepository()
    val eventMap = mutableMapOf<String, EventModel>()

    // camp access and request data
    private val campAccessRepo = CampAccessRepository()
    private val campAccessList = arrayListOf<String>()
    private val campRequestList = arrayListOf<String>()

    // camps data
    private val campsRepo = CampsRepository()

    // adapters
    val campAdapter = CampAdapter(this, arrayListOf())
    val requestAdapter = RequestAdapter(this, arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        title = findViewById(R.id.main_title)
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            user = currentUser
            loadCampsFragment(false)

            setupRepos()
            setupDrawerAndNav()
        } else {
            goToSplashScreenActivity()
        }
    }


    override fun onStop() {
        detachListeners()
        super.onStop()
    }

    //setups
    private fun setupRepos() {
        campAccessRepo.watchCampAccess(user) {
            handleRequestListUpdate(CampAccessRepository.Singleton.campRequestList)
            handleAccessListUpdate(CampAccessRepository.Singleton.campAccessList)
        }
        userEventsRepo.watchUserEvents(user.uid) {
            handleEventUidListUpdate(UserEventsRepository.Singleton.eventUidList)
        }
    }

    private fun setupDrawerAndNav() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.main_layout)

        findViewById<ImageView>(R.id.main_menu_button).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val navView = findViewById<NavigationView>(R.id.main_nav_view)
        navView.setCheckedItem(R.id.main_menu_camps_item)
        navView.setNavigationItemSelectedListener {
            if (navView.checkedItem != it) {
                when (it.itemId) {
                    R.id.main_menu_camps_item -> loadCampsFragment(false)
                    R.id.main_menu_requests_item -> loadRequestsFragment(false)
                    R.id.main_menu_day_item -> loadUserCalendarFragment(false)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        val headerView = navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.main_menu_header_name).text = user.displayName
        headerView.findViewById<TextView>(R.id.main_menu_header_email).text = user.email
        headerView.findViewById<ConstraintLayout>(R.id.main_menu_header_sign_out)
            .setOnClickListener { signOut() }
    }

    // utils
    private fun signOut() {
        buildConfirmDialog(this) {
            FirebaseAuth.getInstance().signOut()
            goToSplashScreenActivity()
        }
    }

    private fun handleEventUidListUpdate(updatedEventUidList: List<String>) {
        // put a listener on new events
        val newEventsUid = updatedEventUidList.filter { !eventUidList.contains(it) }
        for (eventUid in newEventsUid) {
            eventsRepo.watchEvent(eventUid, user.uid, null) {
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

    private fun handleAccessListUpdate(updatedAccessList: List<String>) {
        // put a Listener on new camps
        val newCampsUid = updatedAccessList.filter { !campAccessList.contains(it) }
        for (campUid in newCampsUid) {
            campsRepo.watchCamp(campUid) {
                val camp = CampsRepository.Singleton.campMap[campUid]
                if (camp != null) {
                    campAdapter.updateItem(camp)
                    // update userNameMap
                    UsersRepository.Singleton.userNameMap.putAll(camp.animators)
                }
            }
        }
        // remove listener on removed camp, if not already done
        val removedCampsUid = campAccessList.filter { !updatedAccessList.contains(it) }
        for (campUid in removedCampsUid) {
            campsRepo.stopWatchingCamp(campUid)
            campAdapter.deleteItem(campUid)
        }
        // update accessList
        campAccessList.clear()
        campAccessList.addAll(updatedAccessList)
    }

    private fun handleRequestListUpdate(updatedRequestList: List<String>) {
        // put a Listener on new camps
        val newCampsUid = updatedRequestList.filter { !campRequestList.contains(it) }
        for (campUid in newCampsUid) {
            campsRepo.watchCamp(campUid) {
                val camp = CampsRepository.Singleton.campMap[campUid]
                if (camp != null) {
                    requestAdapter.updateItem(camp)
                    // update userNameMap
                    UsersRepository.Singleton.userNameMap.putAll(camp.animators)
                }
            }
        }
        // remove listener on removed camp, if not already done
        val removedCampsUid = campRequestList.filter { !updatedRequestList.contains(it) }
        for (campUid in removedCampsUid) {
            campsRepo.stopWatchingCamp(campUid)
            requestAdapter.deleteItem(campUid)
        }
        // update accessList
        campRequestList.clear()
        campRequestList.addAll(updatedRequestList)
    }

    private fun detachListeners() {
        campAccessRepo.stopWatchingCampAccess()
        campAccessList.clear()
        campsRepo.stopWatchingAllCamps()

        userEventsRepo.stopWatchingUserEvents()
        eventsRepo.stopWatchingEvents(eventUidList)
        eventUidList.clear()
    }

    // activity/fragment loaders
    fun goToSplashScreenActivity() {
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun goToCampActivity(camp: CampModel) {
        val intent = Intent(this, CampActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("camp", camp)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun loadUserCalendarFragment(addToBackStack: Boolean) {
        title.text = resources.getText(R.string.main_menu_calendar)
        loadFragment(UserCalendarFragment(this), addToBackStack)
    }

    private fun loadCampsFragment(addToBackStack: Boolean) {
        title.text = resources.getText(R.string.main_menu_camps)
        loadFragment(CampsFragment(this), addToBackStack)
    }

    private fun loadRequestsFragment(addToBackStack: Boolean) {
        title.text = resources.getText(R.string.main_menu_requests)
        loadFragment(RequestsFragment(this), addToBackStack)
    }
}