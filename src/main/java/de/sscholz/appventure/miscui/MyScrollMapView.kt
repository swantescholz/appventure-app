package de.sscholz.appventure.miscui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.gms.maps.MapView

// used to put a map than can normally be moved, inside of a scroll view. the map takes precedence.
class MyScrollMapView(p0: Context?, p1: AttributeSet?) : MapView(p0, p1) {
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN ->
                // Disallow ScrollView to intercept touch events.
                this.parent.requestDisallowInterceptTouchEvent(true)

            MotionEvent.ACTION_UP ->
                // Allow ScrollView to intercept touch events.
                this.parent.requestDisallowInterceptTouchEvent(false)
        }
        // Handle MapView's touch events.
        super.onTouchEvent(ev)
        return true
    }
}