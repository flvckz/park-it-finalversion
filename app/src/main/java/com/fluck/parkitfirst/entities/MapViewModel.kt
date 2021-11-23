package com.fluck.parkitfirst.entities

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MapViewModel : ViewModel() {
    var currentLocation: LatLng = LatLng(-34.6103, -58.4305)
}