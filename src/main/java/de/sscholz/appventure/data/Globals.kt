package de.sscholz.appventure.data

import android.content.res.AssetManager
import android.content.res.Resources
import com.google.android.gms.maps.model.LatLng

object Globals {

    val paderbornPosition = LatLng(51.7189, 8.7575)
    const val GOAL_UNCERTAINTY_RADIUS = 150.0
    const val LOCATION_REQUEST_INTERVAL: Long = 10 * 1000
    const val LOCATION_REQUEST_FASTEST_INTERVAL: Long = 5 * 1000
    val SOLUTIONS_THAT_ALWAYS_WORK = setOf("42", "")
    const val RECORD_NEW_POSITION_DISTANCE_THRESHOLD = 6.0
    const val DATABASE_VERSION = 18

    var screenWidth = 0
        set(value) {
            if (field == 0)
                field = value
        }
    var screenHeight = 0
        set(value) {
            if (field == 0)
                field = value
        }

    var resources: Resources? = null
        set(value) {
            if (field == null)
                field = value
        }

    var assets: AssetManager? = null
        set(value) {
            if (field == null)
                field = value
        }


    val worldLocations = hashMapOf(
            "Paderborn" to LatLng(51.7189, 8.7575),
            "Bielefeld" to LatLng(52.0302, 8.5325),
            "Herford " to LatLng(52.1178, 8.6794),
            "Geseke" to LatLng(51.6394, 8.5068),
            "Sydney" to LatLng(-33.8688, 151.2093),
            "Brilon" to LatLng(51.3982, 8.5749)
    )

    const val LOCATION_PERMISSION_REQUEST_CODE = 1
    const val REQUEST_CHECK_SETTINGS = 2

}