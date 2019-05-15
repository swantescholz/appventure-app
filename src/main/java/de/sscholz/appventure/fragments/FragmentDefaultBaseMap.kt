package de.sscholz.appventure.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import de.sscholz.appventure.data.AppData
import de.sscholz.appventure.R


abstract class FragmentDefaultBaseMap : Fragment(), GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener {

    override fun onCameraMove() {

    }

    override fun onCameraIdle() {
        AppData.currentCameraPosition.value = googleMap.cameraPosition.target
    }

    protected lateinit var mMapView: MapView
    protected lateinit var googleMap: GoogleMap

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_touroverview_map, container, false)

        mMapView = rootView.findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)

        mMapView.onResume() // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapView.getMapAsync { mMap ->
            googleMap = mMap
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = true
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.setOnCameraIdleListener(this)
            googleMap.setOnCameraMoveListener(this)
            if (AppData.hasFineLocationPermission.value) {
                // For showing a move to my location button
                googleMap.isMyLocationEnabled = true
            }

            afterMapReady()
        }

        return rootView
    }

    protected abstract fun afterMapReady()

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}