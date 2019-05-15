package de.sscholz.appventure.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import de.sscholz.appventure.R
import de.sscholz.appventure.data.AppData
import de.sscholz.appventure.data.Globals
import de.sscholz.appventure.data.MyRoomDb
import de.sscholz.appventure.data.PastPosition
import de.sscholz.appventure.util.displaySimpleAlert
import de.sscholz.appventure.util.distanceTo
import de.sscholz.appventure.util.nonNullObserve
import de.sscholz.appventure.util.toLatLng
import java.util.*
import kotlin.concurrent.thread

abstract class ActivityLocationMonitor : AppCompatActivity() {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    protected var doingLocationUpdates = false

    private fun createLocationRequestAndCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val currentPosition = p0.lastLocation.toLatLng()
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@ActivityLocationMonitor)
                val prefShowPlayerPath = prefs.getBoolean("checkbox_show_real_player_path", false)
                val distanceToLastPosition = AppData.lastPlayerLocation.value.distanceTo(currentPosition)
                if (prefShowPlayerPath && distanceToLastPosition > Globals.RECORD_NEW_POSITION_DISTANCE_THRESHOLD) {
                    AppData.lastPlayerLocation.value = currentPosition
//                    val (y, x) = AppData.lastPlayerLocation.value.yxDistanceTo(currentPosition)
//                    toast("%.2f | %.2f".format(y, x))
                    if (AppData.currentTour.value != null) {
                        thread {
                            MyRoomDb.INSTANCE?.pastPositionDao()?.insert(PastPosition(null, AppData.currentTour.value!!.title, currentPosition))
                        }
                    }
                }
            }
        }
        locationRequest = LocationRequest()
        locationRequest.interval = Globals.LOCATION_REQUEST_INTERVAL
        locationRequest.fastestInterval = Globals.LOCATION_REQUEST_FASTEST_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this,
                            Globals.REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    displaySimpleAlert(sendEx.toString())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Globals.REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (doingLocationUpdates || !AppData.hasFineLocationPermission.value)
            return
        doingLocationUpdates = true
        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        if (!doingLocationUpdates)
            return
        doingLocationUpdates = false
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequestAndCallback()
        setupLocationPermissions()
        AppData.hasFineLocationPermission.nonNullObserve(this) {
            if (it) {
                startLocationUpdates()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Globals.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    de.sscholz.appventure.util.println("Permission has been denied by user")
                    AppData.hasFineLocationPermission.postValue(false)
                } else {
                    de.sscholz.appventure.util.println("Permission has been granted by user")
                    AppData.hasFineLocationPermission.postValue(true)
                }
            }
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Globals.LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun setupLocationPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.permission_request_text))
                        .setTitle(getString(R.string.permission_request_title))
                builder.setPositiveButton("OK") { _, _ -> makeRequest() }
                builder.create().show()
            } else {
                makeRequest()
            }
        } else {
            AppData.hasFineLocationPermission.postValue(true)
        }
    }
}