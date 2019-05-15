package de.sscholz.appventure.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayout
import de.sscholz.appventure.*
import de.sscholz.appventure.data.AppData
import de.sscholz.appventure.data.Globals
import de.sscholz.appventure.data.MyRoomDb
import de.sscholz.appventure.data.Tour
import de.sscholz.appventure.fragments.FragmentTourOverviewList
import de.sscholz.appventure.fragments.FragmentTourOverviewMap
import de.sscholz.appventure.util.nonNullObserve
import de.sscholz.appventure.util.nonNullObserveOnce
import de.sscholz.appventure.util.screenHeight
import de.sscholz.appventure.util.screenWidth
import kotlinx.android.synthetic.main.activity_main.*


class ActivityMain : ActivityLocationMonitor(), FragmentTourOverviewList.OnListFragmentInteractionListener {


    // callback for the recycler view list tour click
    override fun onListFragmentInteraction(tour: Tour) {
        val intent = Intent(this, ActivityTourDetails::class.java)
        AppData.currentTour.value = tour
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, ActivitySettings::class.java))
            }
            R.id.action_help -> {
                startActivity(Intent(this, ActivityHelp::class.java))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // important initialization of "global" stuff: database and resource setting in particular
    fun doVeryFirstThingsThatNeedToBeDoneFirst() {
        Globals.resources = resources
        Globals.assets = assets
        Globals.screenWidth = screenWidth
        Globals.screenHeight = screenHeight
        MyRoomDb.getInstanceSave(applicationContext)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        doVeryFirstThingsThatNeedToBeDoneFirst()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initToolbar()

        val adapter = MainTabPageAdapter(supportFragmentManager)
        view_pager.adapter = adapter
        tab_layout.setupWithViewPager(view_pager)
        tab_layout.getTabAt(0)!!.setIcon(R.drawable.ic_flag_24dp)
        tab_layout.getTabAt(1)!!.setIcon(R.drawable.ic_map_24dp)

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        sortToursByDistanceToCamera()

        AppData.currentCameraPosition.nonNullObserve(this) {
            sortToursByDistanceToCamera()
        }

    }


    private fun sortToursByDistanceToCamera() {
        AppData.sortToursByDistanceTo(AppData.currentCameraPosition.value)
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        AppData.appStartCounter.nonNullObserveOnce { newValue ->
            //            supportActionBar!!.title = getString(R.string.app_name) + " $newValue"
            AppData.appStartCounter.value = newValue!! + 1
        }
    }

    class MainTabPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? = when (position) {
            0 -> FragmentTourOverviewList.newInstance(columnCount = 1)
            1 -> FragmentTourOverviewMap()
            else -> null
        }

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            0 -> Globals.resources!!.getString(R.string.activity_main_tab_tours)
            1 -> Globals.resources!!.getString(R.string.activity_main_tab_map)
            else -> ""
        }

        override fun getCount(): Int = 2
    }


}

