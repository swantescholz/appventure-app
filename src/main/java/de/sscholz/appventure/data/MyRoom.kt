package de.sscholz.appventure.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import de.sscholz.appventure.data.Globals.DATABASE_VERSION
import java.util.*


@Entity(tableName = "PastPosition")
data class PastPosition(
        @PrimaryKey(autoGenerate = true)
        var id: Long?,
        var tourId: String,
        val position: LatLng)

@Entity(tableName = "SavedValueEntry")
data class SavedValueEntry(
        @PrimaryKey
        var id: String,
        val value: String)

@Entity(tableName = "TourProgress")
data class TourProgress(
        @PrimaryKey
        var tourId: String,
        val progress: Int)


@Dao
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg entity: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIfNotPresent(vararg entity: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: T)

    @Delete
    fun delete(entity: T)
}

@Dao
interface TourProgressDao : BaseDao<TourProgress> {

    @Query("SELECT * from TourProgress")
    fun getAll(): LiveData<List<TourProgress>>

    @Query("SELECT progress from TourProgress WHERE tourId == :tourId LIMIT 1")
    fun getByTourId(tourId: String): LiveData<Int>

    @Query("DELETE from TourProgress")
    fun deleteAll()

    @Query("DELETE FROM TourProgress WHERE tourId == :tourId")
    fun deleteByTourId(tourId: String)
}

@Dao
interface PastPositionDao : BaseDao<PastPosition> {

    @Query("SELECT * from PastPosition")
    fun getAll(): LiveData<List<PastPosition>>

    @Query("SELECT * from PastPosition WHERE tourId == :tourId")
    fun getByTourId(tourId: String): LiveData<List<PastPosition>>

    @Query("DELETE from PastPosition")
    fun deleteAll()

    @Query("DELETE FROM PastPosition WHERE tourId == :tourId")
    fun deleteAllWithTourId(tourId: String)
}

@Dao
interface SavedValueDao : BaseDao<SavedValueEntry> {

    @Query("SELECT * from SavedValueEntry")
    fun getAll(): List<SavedValueEntry>

    @Query("SELECT * from SavedValueEntry where id == :id")
    fun getById(id: String): List<SavedValueEntry>

    @Query("DELETE from SavedValueEntry")
    fun deleteAll()
}

@Database(entities = [(TourProgress::class), (SavedValueEntry::class), (PastPosition::class)], version = DATABASE_VERSION, exportSchema = false)
@TypeConverters(MyConverters::class)
abstract class MyRoomDb : RoomDatabase() {
    abstract fun savedValueDao(): SavedValueDao
    abstract fun pastPositionDao(): PastPositionDao
    abstract fun tourProgressDao(): TourProgressDao

    companion object {
        var INSTANCE: MyRoomDb? = null

        val instance: MyRoomDb
            get() = INSTANCE!!

        fun getInstanceSave(applicationContext: Context): MyRoomDb {
            if (INSTANCE == null) {
                synchronized(MyRoomDb::class) {
                    INSTANCE = Room.databaseBuilder(applicationContext,
                            MyRoomDb::class.java, "MyRoomDb$DATABASE_VERSION.db")
                            .fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE!!
        }
    }
}