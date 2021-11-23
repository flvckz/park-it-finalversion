package com.fluck.parkitfirst.entities

class UserLog(var uid: String, email: String, pass: String, vehicle: String) {

    var pass : String = ""
    var email : String = ""
    var vehicle : String = ""

    init {

        this.pass = pass.toString()
        this.email = email.toString()
        this.vehicle = vehicle
    }
}