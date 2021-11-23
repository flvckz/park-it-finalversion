package com.fluck.parkitfirst.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.fluck.parkitfirst.MapActivity
import com.fluck.parkitfirst.R
import com.fluck.parkitfirst.entities.LocationInfo
import com.fluck.parkitfirst.entities.MarkersRetrieve
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.location.LocationCallback

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener


class FragmentMapBackup : Fragment(), OnMapReadyCallback {

    lateinit var v: View
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()
    private lateinit var shareLocationBtn: Button
    private lateinit var delLocationBtn: Button
    lateinit var storageReference: StorageReference
    val loc: MutableList<LocationInfo> = arrayListOf()
    private val map: MutableMap<String, Any> = HashMap()
    private lateinit var lastLocation: Location
    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private var lastMarker: Marker? = null
    private val SPLASH_TIME_OUT : Long = 1000
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_map_backup, container, false)
        setupLocClient()
        shareLocationBtn = v.findViewById(R.id.btn_find_location)
        delLocationBtn = v.findViewById(R.id.btn_del_marker)
        storageReference = FirebaseStorage.getInstance().getReference("Markers")

        val actionBar = (activity as MapActivity).supportActionBar

        actionBar!!.setDisplayShowTitleEnabled(false)
        actionBar!!.setTitle("")

        return v
    }

    // use it to request location updates and get the latest location


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapBU) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        // Construct a FusedLocationProviderClient.
        db.collection("Markers").document().delete()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap //initialise map
        mMap.clear()
        //mMap.uiSettings.isZoomControlsEnabled = true

        db.collection("Markers")
            .addSnapshotListener{ result, e ->
                if (e != null){
                    Log.w(TAG, "Listen failed. ", e)
                    return@addSnapshotListener
                }
                loc.clear()
                loc.addAll(result!!.toObjects(LocationInfo::class.java))
                for (LocationInfo in loc){

                    //mMap.clear()

                    val geoPosition = LatLng(LocationInfo.latitude, LocationInfo.longitude)

                    //if (lastMarker != null)
                    //    lastMarker!!.remove()

                    /*lastMarker = */mMap.addMarker(MarkerOptions().position(geoPosition).title(getDirection(geoPosition)))
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(geoPosition))
                }

            }
        setUpMap()
        //new app 22nov
    }

    private fun getDirection(latLng: LatLng) : String {
        val geocoder = Geocoder(context)
        val directions: List<Address>?
        val firstDirection: Address
        var addressTxt = ""

        try {
            directions = geocoder.getFromLocation(
                latLng.latitude, latLng.longitude, 1
            )
            if (directions != null && directions.isNotEmpty()) {
                firstDirection = directions[0]

                //Multiline direction
                if (firstDirection.maxAddressLineIndex > 0) {
                    for (i in 0..firstDirection.maxAddressLineIndex) {
                        addressTxt += firstDirection.getAddressLine(i) + "\n"
                    }
                }
                //Principal y secundario
                else {
                    addressTxt += firstDirection.thoroughfare + ", " + firstDirection.subThoroughfare + "\n"
                }
            }
        } catch (e : Exception) {
            addressTxt = "No address found."
        }
        return addressTxt
    }

    private fun setupLocClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    // prompt the user to grant/deny access

    companion object {
        private const val REQUEST_LOCATION = 1 //request code to identify specific permission request
        private const val TAG = "FragmentMapBackup" // for debugging
    }

    private fun uploadData(data: Map<String, Any>) {
        userId?.let {
            db!!.collection("Markers").document(it)
                .set(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "Ubication added to map", Toast.LENGTH_SHORT).show()
                    shareLocationBtn?.isEnabled = true
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Issue on adding ubication", Toast.LENGTH_SHORT).show()
                    shareLocationBtn?.isEnabled = true

                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val location = fusedLocationClient.lastLocation
        location.addOnSuccessListener { location ->
            if(location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)

                map["latitude"] = location.latitude
                map["longitude"] = location.longitude
                uploadData(map)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
            }
        }
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        shareLocationBtn.setOnClickListener{
            getLastLocation()
        }

        delLocationBtn.setOnClickListener {
            db.collection("Markers").document(userId!!).delete()
            lastMarker?.remove()

            Handler().postDelayed(

                {
                    val action = FragmentMapBackupDirections.actionFragmentMapBackupToFragmentWait()
                    v.findNavController().navigate(action)
                }, SPLASH_TIME_OUT)
        }
    }
}