package com.fluck.parkitfirst.entities

class UserInfo (var uid: String, name : String, surname : String, vehicle : String) {
    var name : String = ""
    var surname : String = ""
    var vehicle : String = ""

    init {
        this.name = name
        this.surname = surname
        this.vehicle = vehicle
    }

    class Constants{
        companion object {
            val Sedan = "Sedan"
            val SUV = "SUV"
            val Hatchback = "Hatchback"
            val Compact = "Compact"
            val Pickup = "Pickup"
        }
    }

}