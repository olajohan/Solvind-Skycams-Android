package com.solvind.skycams.app.presentation.home


import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addMarker
import com.solvind.skycams.app.domain.model.Skycam
import timber.log.Timber
import javax.inject.Inject

class MapMarkerHandler @Inject constructor() {

    private val mSkycamMarkers = mutableMapOf<String, Marker>()
    private var mSkycamMarkersIsVisible = true

    fun addSkycamMarkers(googleMap: GoogleMap, skycamList: List<Skycam>) {
        skycamList.forEach { skycam ->
            val marker = googleMap.addMarker {
                position(
                    LatLng(
                        skycam.location.coordinates.latitude,
                        skycam.location.coordinates.longitude
                    )
                )
            }
            marker.tag = skycam
            mSkycamMarkers[skycam.skycamKey] = marker
            Timber.i("Marker added ${skycam.skycamKey}")
        }
    }

    fun skycamMarkersIsVisible() = mSkycamMarkers.values.forEach {
        it.isVisible = !mSkycamMarkersIsVisible
    }
}

