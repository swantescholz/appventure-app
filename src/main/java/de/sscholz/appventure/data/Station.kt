package de.sscholz.appventure.data

import com.google.android.gms.maps.model.LatLng
import de.sscholz.appventure.util.distanceTo

data class Station(
        val title: String,
        val position: LatLng,
        val description: String,
        var imageUri: String,
        val puzzleSolutions: List<String>,
        val messageAfterSolving: String? = null) {

    fun distanceTo(other: Station): Double {
        return position.distanceTo(other.position)
    }

}
