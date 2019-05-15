package de.sscholz.appventure.fragments

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import de.sscholz.appventure.data.AppData
import de.sscholz.appventure.data.Tour
import de.sscholz.appventure.util.enumerate
import de.sscholz.appventure.util.formatEllipsis
import de.sscholz.appventure.util.nonNullObserve


class FragmentTourOverviewMap : FragmentDefaultBaseMap() {

    private lateinit var tours: ArrayList<Tour>

    override fun afterMapReady() {
        tours = AppData.tours.value!!
        AppData.tours.nonNullObserve(this) {
            tours = it!!
            updateMarkers()
        }
        // For zooming automatically to the last location
        val cameraPosition = CameraPosition.Builder().target(AppData.currentCameraPosition.value).zoom(12f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        var lastOpenned: Marker? = null

        googleMap.setOnMarkerClickListener(OnMarkerClickListener { marker ->
            if (lastOpenned != null) {
                lastOpenned!!.hideInfoWindow()
                if (lastOpenned == marker) {
                    lastOpenned = null
                    return@OnMarkerClickListener true
                }
            }
            marker.showInfoWindow()
            lastOpenned = marker
            true
        })
    }

    private fun updateMarkers() {
        googleMap.clear()
        val iconGenerator = IconGenerator(context)
        enumerate(tours).forEach { (i, tour) ->
            iconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
            val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("${i + 1}"))
            googleMap.addMarker(MarkerOptions().position(tour.startPosition).title(formatEllipsis(tour.title, 40)).icon(icon).snippet(formatEllipsis(tour.description, 50)))
        }
    }
}