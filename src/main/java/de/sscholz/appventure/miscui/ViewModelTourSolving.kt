package de.sscholz.appventure.miscui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import de.sscholz.appventure.data.*
import de.sscholz.appventure.util.nonNullObserveOnce
import java.util.*
import kotlin.concurrent.thread

class ViewModelTourSolving : ViewModel() {
    var pastPositionDao: PastPositionDao
    var tourProgressDao: TourProgressDao
    val currentTour = AppData.currentTour.value!!
    val currentStationIndex = DefaultMutableLiveData(0)
    val pastPositions: LiveData<List<PastPosition>>
    var unveilNextStation = MutableLiveData<Boolean>()

    val currentStation: Station
        get() = currentTour.stations[currentStationIndex.value]
    val currentTourProgress: LiveData<Int>

    init {
        pastPositionDao = MyRoomDb.instance.pastPositionDao()
        pastPositions = pastPositionDao.getByTourId(currentTour.title)
        tourProgressDao = MyRoomDb.instance.tourProgressDao()
        thread {
            tourProgressDao.insertIfNotPresent(TourProgress(currentTour.title, 0))
        }
        currentTourProgress = tourProgressDao.getByTourId(currentTour.title)
        currentTourProgress.nonNullObserveOnce {
            currentStationIndex.value = it
        }
    }

    fun addCurrentPosition(currentPosition: LatLng) {
        pastPositionDao.insert(PastPosition(null, currentTour.title, currentPosition))
    }

    fun setNewStationIndex(newStationIndex: Int) {
        if (newStationIndex > currentTourProgress.value!!) {
            thread {
                tourProgressDao.insert(TourProgress(currentTour.title, newStationIndex))
            }
        }
        currentStationIndex.value = newStationIndex
    }

    fun resetTourProgress() {
        deletePositionHistory()
        thread {
            tourProgressDao.insert(TourProgress(currentTour.title, 0))
        }
        currentStationIndex.value = 0
    }

    fun deletePositionHistory() {
        thread {
            pastPositionDao.deleteAllWithTourId(currentTour.title)
        }
    }
}