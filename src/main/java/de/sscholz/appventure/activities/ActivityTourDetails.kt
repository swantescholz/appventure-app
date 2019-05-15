package de.sscholz.appventure.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import de.sscholz.appventure.*
import de.sscholz.appventure.data.AppData
import de.sscholz.appventure.data.MyRoomDb
import de.sscholz.appventure.data.Tour
import de.sscholz.appventure.data.TourProgress
import de.sscholz.appventure.util.*
import kotlinx.android.synthetic.main.activity_tour_details.*
import kotlin.concurrent.thread


class WorkaroundMapFragment : SupportMapFragment() {
    private var mListener: OnTouchListener? = null

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, savedInstance: Bundle?): View {
        val layout = super.onCreateView(layoutInflater, viewGroup, savedInstance)

        val frameLayout = TouchableWrapper(activity!!)

        frameLayout.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.transparent))

        (layout as ViewGroup).addView(frameLayout,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        return layout
    }

    fun setListener(listener: OnTouchListener) {
        mListener = listener
    }

    interface OnTouchListener {
        fun onTouch()
    }

    inner class TouchableWrapper(context: Context) : FrameLayout(context) {

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mListener!!.onTouch()
                MotionEvent.ACTION_UP -> mListener!!.onTouch()
            }
            return super.dispatchTouchEvent(event)
        }
    }
}

class ActivityTourDetails : ActivityLocationMonitor() {


    private lateinit var mMapFragment: WorkaroundMapFragment
    private lateinit var mView: View
    private lateinit var googleMap: GoogleMap
    private lateinit var tourProgress: LiveData<Int>
    lateinit var tour: Tour

    @SuppressLint("SetTextI18n", "MissingPermission", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_details)
        setSupportActionBar(toolbar)
        tour = AppData.currentTour.value!!
        tourProgress = MyRoomDb.instance.tourProgressDao().getByTourId(tour.title)


        button_tour_start.setOnClickListener {
            val intent = Intent(this, ActivityTourSolving::class.java)
            startActivity(intent)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Glide.with(this)
                .load(completeAssetUri(tour.imageUri))
                .apply(RequestOptions()
//                        .placeholder(R.drawable.ic_image_placeholder_24dp)
                        .error(R.drawable.ic_error_24dp))
                .into(tour_image)

        tour_title.text = tour.title
        tour_stats.text = "${formatDistance(tour.startPosition.distanceTo(AppData.currentCameraPosition.value))} entfernt\n${tour.numberOfStations} Stationen\n" +
                "${formatDistance(tour.completeDistance)} / ${formatTime(tour.estimatedCompletionTime)}"
        tour_rankings.text = "Schwierigkeit: ${tour.difficultyRanking}/5\n" +
                "Story: ${tour.storyRanking}/5\n" +
                "Landschaft: ${tour.landscapeRanking}/5"
        tour_description.text = tour.description

        mMapFragment = fragment_map as WorkaroundMapFragment
        mView = mMapFragment.view!!
        mMapFragment.setListener(object : WorkaroundMapFragment.OnTouchListener {
            override fun onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true)
            }
        })

        mView.layoutParams.apply {
            width = screenWidth
            height = screenWidth
            mView.layoutParams = this
        }
        mMapFragment.onCreate(savedInstanceState)
        mMapFragment.onResume() // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapFragment.getMapAsync { mMap: GoogleMap ->
            googleMap = mMap
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = true
            googleMap.uiSettings.isZoomControlsEnabled = true
            if (AppData.hasFineLocationPermission.value) {
                // For showing a move to my location button
                googleMap.isMyLocationEnabled = true
            }
            tourProgress.observe(this) { newStationIndex: Int? ->
                if (newStationIndex == null) {
                    thread {
                        MyRoomDb.instance.tourProgressDao().insert(TourProgress(tour.title, 0))
                    }
                    return@observe
                }
                if (newStationIndex > 0) {
                    button_tour_start.text = getString(R.string.button_tour_continue) + " (${newStationIndex + 1}/${tour.numberOfStations})"
                } else {
                    button_tour_start.text = getString(R.string.button_tour_start)
                }
                putStationMarkers(newStationIndex)
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@ActivityTourDetails)
                val prefGoal = prefs.getString("pref_last_station_visible", "no")
                val positionsToLookAt = tour.getPositionsToLookAt(newStationIndex, prefGoal)
                googleMap.animateCamera(cameraUpdateForIncludingAllLocations(mView.layoutParams.width, mView.layoutParams.height, positionsToLookAt))
            }
        }
    }


    private fun putStationMarkers(newStationIndex: Int) {
        googleMap.putStationMarkers(this, tour, newStationIndex)
    }

}
