package com.fluck.parkitfirst.entities

class LocationInfo (var id: String, val latitude: Double, val longitude: Double) {

    constructor(): this("", 0.0, 0.0)
}