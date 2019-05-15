package de.sscholz.appventure.util

import android.app.Activity
import android.graphics.Color
import android.location.Location
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.google.maps.android.ui.IconGenerator
import de.sscholz.appventure.R
import de.sscholz.appventure.data.Globals
import de.sscholz.appventure.data.Station
import de.sscholz.appventure.data.Tour
import java.util.*

fun Tour.getPositionsToLookAt(newStationIndex: Int, prefGoal: String): MutableList<LatLng> {
    val positionsToLookAt = mutableListOf(startPosition)
    stations.map { it.position }.take(newStationIndex).forEach { positionsToLookAt.add(it) }
    if (prefGoal != "no") {
        positionsToLookAt.add(stations.last().position)
    }
    return positionsToLookAt
}

fun GoogleMap.putStationMarkers(activity: Activity, tour: Tour, newStationIndex: Int) {
    this.clear()
    val iconGenerator = IconGenerator(activity)
    iconGenerator.setStyle(IconGenerator.STYLE_GREEN)
    val startIcon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getString(R.string.map_start_marker)))
    addMarker(MarkerOptions().position(tour.startPosition).title(getString(R.string.map_start_marker_title)).icon(startIcon))
    val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
    val prefGoal = prefs.getString("pref_last_station_visible", "no")
    enumerate(tour.stations, startIndex = 1).take(Math.min(tour.numberOfStations - 1, newStationIndex)).forEach { (i, station) ->
        iconGenerator.setStyle(IconGenerator.STYLE_BLUE)
        val stationIcon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("$i"))
        addMarker(station.toMarkerOptions(stationIcon))
    }
    val polylinePositions = mutableListOf(tour.startPosition)
    tour.stations.map { it.position }.take(newStationIndex).forEach { polylinePositions.add(it) }
    addPolyline(PolylineOptions()
            .add(*polylinePositions.toTypedArray())
            .width(5f)
            .color(Color.RED))
    if (prefGoal == "maybe") {
        addCircle(CircleOptions()
                .center(tour.goalUncertaintyCenter!!)
                .radius(Globals.GOAL_UNCERTAINTY_RADIUS)
                .strokeWidth(5f)
                .strokeColor(R.color.Bisque)
                .fillColor(R.color.Chocolate))
    }
    if (prefGoal == "yes" || newStationIndex >= tour.numberOfStations) {
        iconGenerator.setStyle(IconGenerator.STYLE_GREEN)
        val endIcon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getString(R.string.map_end_marker)))
        addMarker(tour.stations.last().toMarkerOptions(endIcon))
    }
}

fun Station.toMarkerOptions(icon: BitmapDescriptor): MarkerOptions =
        MarkerOptions().position(position).title(formatEllipsis(title, 40)).icon(icon).snippet(formatEllipsis(description, 50))

fun LatLng.distanceTo(other: LatLng) = SphericalUtil.computeDistanceBetween(this, other)
// returns (y,x). y=how many meter north to go from this to other, x=how much many meter east to go from this to other
fun LatLng.yxDistanceTo(other: LatLng): Pair<Double, Double> {
    val y = this.distanceTo(LatLng(other.latitude, this.longitude)) * if (other.latitude > this.latitude) 1 else -1
    val x = this.distanceTo(LatLng(this.latitude, other.longitude)) * if (other.longitude > this.longitude) 1 else -1
    return Pair(y, x)
}

fun LatLng.randomLatLngWithinRadius(radiusInMeter: Double): LatLng {
    while (true) {
        val x0 = this.latitude
        val y0 = this.longitude
        val random = Random()
        val radiusInDegrees = radiusInMeter / 111000.0
        val u = random.nextDouble()
        val v = random.nextDouble()
        val w = radiusInDegrees * Math.sqrt(u)
        val t = 2.0 * Math.PI * v
        val x = w * Math.cos(t)
        val y = w * Math.sin(t)
        // Adjust the x-coordinate for the shrinking of the east-west distances
        val new_x = x / Math.cos(y0)
        val foundLatitude = new_x + x0
        val foundLongitude = y + y0
        val randomPos = LatLng(foundLatitude, foundLongitude)
        if (randomPos.distanceTo(this) < radiusInMeter)
            return randomPos
    }
}


fun Location.toLatLng() = LatLng(this.latitude, this.longitude)
fun cameraUpdateForIncludingAllLocations(mapWidthInPx: Int, mapHeightInPx: Int, locations: Iterable<LatLng>,
                                         paddingInPartsOfWidth: Double = 0.15): CameraUpdate? {
    val builder = LatLngBounds.Builder()
    locations.forEach {
        builder.include(it)
    }
    val bounds = builder.build()

    //Setting the width and height of your screen
    val padding = (mapWidthInPx * paddingInPartsOfWidth).toInt() // offset from edges of the map x% of screen

    return CameraUpdateFactory.newLatLngBounds(bounds, mapWidthInPx, mapHeightInPx, padding)
}