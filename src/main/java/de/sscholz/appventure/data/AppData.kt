package de.sscholz.appventure.data

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import de.sscholz.appventure.util.convertYamlToJson
import de.sscholz.appventure.util.readAssetTextFile
import java.util.*

/**
 * singleton to store anything that should survive activities randomly being closed
 * some of these are stored via the room database to survive even the app closing/crashing
 *
 */
object AppData {
    val tours = MutableLiveData<ArrayList<Tour>>()
    var currentTour = MutableLiveData<Tour>()
    var hasFineLocationPermission = SavedValue("hasFineLocationPermission", false, Boolean::class.java)

    val currentCameraPosition = SavedValue("currentCameraPosition", Globals.paderbornPosition, LatLng::class.java)
    val appStartCounter = SavedValue("appStartCounter", 5, Int::class.java)
    val lastPlayerLocation = SavedValue("lastPlayerLocation", Globals.paderbornPosition, LatLng::class.java)

    init {
        tours.value = ArrayList()
        currentCameraPosition.value = Globals.paderbornPosition
        loadMyStories()
    }

    private fun loadMyStories() {
        val storyNames = Gson().fromJson(convertYamlToJson(Globals.assets!!.readAssetTextFile("stories.yaml")), ArrayList::class.java)
        storyNames.forEach { storyName ->
            val yamlPath = "$storyName/$storyName.yaml"
            val json = convertYamlToJson(Globals.assets!!.readAssetTextFile(yamlPath))
            val tour = Gson().fromJson(json, Tour::class.java)
            tour.imageUri = "$storyName/${tour.imageUri}"
            tour.stations.forEach { station ->
                station.imageUri = "$storyName/${station.imageUri}"
            }
            addTour(tour)
        }
    }

    // notifies observes of change!
    fun sortToursByDistanceTo(position: LatLng) {
        val newTours = ArrayList(tours.value)
        newTours.sortBy { it.distanceTo(position) }
        tours.value = newTours
    }

    private fun addTour(tour: Tour) {
        tours.value?.add(tour)
    }


}
