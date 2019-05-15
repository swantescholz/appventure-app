package de.sscholz.appventure.fragments

import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.model.PolylineOptions
import de.sscholz.appventure.miscui.ViewModelTourSolving
import de.sscholz.appventure.util.*
import kotlinx.android.synthetic.main.fragment_tour_solving_map.*

class FragmentTourSolvingMap : FragmentDefaultBaseMap() {

    private lateinit var viewModel: ViewModelTourSolving

    override fun afterMapReady() {
        viewModel = ViewModelProviders.of(activity!!).get(ViewModelTourSolving::class.java)
        viewModel.pastPositions.nonNullObserve(this) { updateMarkers(viewModel.currentStationIndex.value) }
        viewModel.currentStationIndex.nonNullObserve(this) { updateMarkers(viewModel.currentStationIndex.value) }
        viewModel.currentStationIndex.nonNullObserveOnce { nextStationIndex ->
            updateCamera(nextStationIndex)
        }
        viewModel.unveilNextStation.nonNullObserve(this) { unveil ->
            if (unveil) {
                updateMarkers(viewModel.currentStationIndex.value)
                updateCamera(viewModel.currentStationIndex.value)
            }
        }
    }

    private fun updateCamera(nextStationIndex: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val prefGoal = prefs.getString("pref_last_station_visible", "no")
        val positionsToLookAt = viewModel.currentTour.getPositionsToLookAt(nextStationIndex + viewModel.unveilNextStation.getOrDefault(false), prefGoal)
        googleMap.animateCamera(cameraUpdateForIncludingAllLocations(mapView.width, mapView.height, positionsToLookAt))
    }

    private fun updateMarkers(newStationIndex: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val prefShowPlayerPath = prefs.getBoolean("checkbox_show_real_player_path", false)
        googleMap.putStationMarkers(activity!!, viewModel.currentTour, newStationIndex + viewModel.unveilNextStation.getOrDefault(false))
        if (viewModel.pastPositions.value != null && prefShowPlayerPath) {
            googleMap.addPolyline(PolylineOptions()
                    .add(*viewModel.pastPositions.value!!.map { it.position }.toTypedArray())
                    .width(3f)
                    .color(Color.BLUE))
        }
    }

    companion object {

        fun newInstance(): FragmentTourSolvingMap {
            val fragment = FragmentTourSolvingMap()
            return fragment
        }
    }
}