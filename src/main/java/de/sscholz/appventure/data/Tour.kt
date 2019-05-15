package de.sscholz.appventure.data

import com.google.android.gms.maps.model.LatLng
import de.sscholz.appventure.util.distanceTo
import de.sscholz.appventure.util.random
import de.sscholz.appventure.util.randomLatLngWithinRadius

class Tour(val title: String, val description: String, var imageUri: String, val stations: List<Station>,
           val startPosition: LatLng = Globals.paderbornPosition) {
    companion object {
        const val MAX_METRIC_SCORE: Int = 5
    }

    val numberOfStations: Int
        get() = stations.size
    // returns the whole distance in meter
    val completeDistance: Double
        get() = (1 until numberOfStations).map { stations[it].distanceTo(stations[it - 1]) }.sum() + stations[0].position.distanceTo(startPosition)

    val difficultyRanking = (1..MAX_METRIC_SCORE).random()
    val storyRanking = (1..MAX_METRIC_SCORE).random()
    val landscapeRanking = (1..MAX_METRIC_SCORE).random()
    val estimatedCompletionTime: Int
        get() = numberOfStations * 32
    val endPoition: LatLng
        get() = stations[stations.size - 1].position

    var goalUncertaintyCenter: LatLng? = null
        get() {
            if (field == null) {
                field = endPoition.randomLatLngWithinRadius(Globals.GOAL_UNCERTAINTY_RADIUS)
            }
            return field
        }

    fun distanceTo(position: LatLng) = this.startPosition.distanceTo(position)

}