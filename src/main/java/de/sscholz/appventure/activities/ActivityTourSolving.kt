package de.sscholz.appventure.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import de.sscholz.appventure.*
import de.sscholz.appventure.data.*
import de.sscholz.appventure.fragments.FragmentTourSolvingMain
import de.sscholz.appventure.fragments.FragmentTourSolvingMap
import de.sscholz.appventure.miscui.ViewModelTourSolving
import de.sscholz.appventure.util.snackbar
import kotlinx.android.synthetic.main.activity_tour_solving.*
import kotlin.concurrent.thread

class ActivityTourSolving : ActivityLocationMonitor() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    lateinit var tour: Tour
    private lateinit var viewModel: ViewModelTourSolving

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewModelTourSolving::class.java)
        setContentView(R.layout.activity_tour_solving)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        tour = AppData.currentTour.value!!
        toolbar.title = tour.title
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        view_pager.adapter = mSectionsPagerAdapter
        tab_layout.setupWithViewPager(view_pager)
        tab_layout.getTabAt(0)!!.setIcon(R.drawable.ic_flag_24dp)
        tab_layout.getTabAt(1)!!.setIcon(R.drawable.ic_map_24dp)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_past_positions -> {
                viewModel.deletePositionHistory()
                snackbar("Gespeicherte Positionsdaten gelöscht.")
            }
            R.id.action_restart_tour -> {
                viewModel.resetTourProgress()
                snackbar("Tour neugestartet.")
            }
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                NavUtils.navigateUpFromSameTask(this)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tour_solving, menu)
        return true
    }


    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? = when (position) {
            0 -> FragmentTourSolvingMain()
            1 -> FragmentTourSolvingMap.newInstance()
            else -> null
        }

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            0 -> Globals.resources!!.getString(R.string.activity_solving_tab_main)
            1 -> Globals.resources!!.getString(R.string.activity_solving_tab_map)
            else -> ""
        }

        override fun getCount(): Int = 2
    }


}
