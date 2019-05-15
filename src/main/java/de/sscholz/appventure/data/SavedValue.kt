package de.sscholz.appventure.data

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import de.sscholz.appventure.util.loge
import kotlin.concurrent.thread

/**
 * class to save a value immediately in the room database as string (via Gson)
 * using the liveData you can attach additional observers which will be triggered when the value changes
 * @param id: the key to be used when storing this value in the database
 */
class SavedValue<T>(val id: String, private val defaultValue: T, clazz: Class<T>) : MutableLiveData<T>() {

    private val savedValueDao = MyRoomDb.instance.savedValueDao()

    override fun getValue(): T {
        return super.getValue() ?: defaultValue
    }

    init {

        observeForever { changedData ->
            thread {
                loge("observe $id", value, changedData)
                savedValueDao.insert(SavedValueEntry(id = id, value = Gson().toJson(changedData)))
            }
        }
        thread {
            savedValueDao.getById(id).let { list ->
                if (list.size == 1) {
                    val newValue = Gson().fromJson(list[0].value, clazz)
                    postValue(newValue)
                    loge("load $id", newValue)
                } else if (list.size > 1) {
                    throw RuntimeException("too many values with the same key saved")
                } else {
                    postValue(defaultValue)
                }

            }
        }
    }

    override fun toString(): String {
        return "$id=$value"
    }

}