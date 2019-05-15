package de.sscholz.appventure.data

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.util.*

class MyConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromLatLng(latLng: LatLng?): String? {
        if (latLng == null)
            return null
        return Gson().toJson(latLng)
    }

    @TypeConverter
    fun toLatLng(s: String?): LatLng? {
        if (s == null)
            return null
        return Gson().fromJson(s, LatLng::class.java)
    }

}